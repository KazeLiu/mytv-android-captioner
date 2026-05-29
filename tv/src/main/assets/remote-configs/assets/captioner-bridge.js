;(function () {
  const CAPTIONER_HASH = '#/captioner'
  const overlayId = 'captioner-bridge-root'
  const styleId = 'captioner-bridge-style'

  const sourceLanguages = [
    ['auto', '自动识别'],
    ['zh', '中文'],
    ['en', '英文'],
    ['ja', '日文'],
    ['ko', '韩文'],
    ['yue', '粤语'],
    ['fr', '法文'],
    ['de', '德文'],
    ['es', '西文'],
    ['ru', '俄文'],
  ]
  const targetLanguages = [
    ['none', '不翻译'],
    ['ChineseSimplified', '简体中文'],
    ['ChineseTraditional', '繁体中文'],
    ['English', '英文'],
    ['Japanese', '日文'],
    ['Korean', '韩文'],
    ['French', '法文'],
    ['German', '德文'],
    ['Spanish', '西文'],
    ['Russian', '俄文'],
  ]
  const chineseScripts = [
    ['simplified', '简体中文'],
    ['traditional', '繁体中文'],
    ['original', '保留原始输出'],
  ]
  const durationOptions = [3, 5, 8, 10, 15].map((value) => [value * 1000, `${value}秒`])
  const displayDurationOptions = [3, 5, 7, 10, 15].map((value) => [value * 1000, `${value}秒`])
  const beamOptions = [1, 2, 3, 4, 5].map((value) => [value, `beam ${value}`])
  const textColors = [
    ['WHITE', '白色'],
    ['YELLOW', '柔黄'],
    ['CYAN', '青蓝'],
    ['GREEN', '浅绿'],
  ]
  const backgroundColors = [
    ['BLACK', '深黑'],
    ['TRANSLUCENT', '半透明'],
    ['BLUE', '深蓝'],
    ['TRANSPARENT', '无背景'],
  ]
  const positions = [
    ['TOP', '顶部'],
    ['CENTER', '居中'],
    ['BOTTOM', '底部'],
  ]
  const textAligns = [
    ['LEFT', '居左'],
    ['CENTER', '居中'],
    ['RIGHT', '居右'],
  ]

  let configs = null
  let loading = false
  let modelOptions = null
  let modelLoadPromise = null
  let modelLoadError = ''
  let modelOptionsServerUrl = ''
  let modelLoadToken = 0

  function ensureStyle() {
    if (document.getElementById(styleId)) return
    const style = document.createElement('style')
    style.id = styleId
    style.textContent = `
      .captioner-bridge {
        position: fixed;
        inset: 0;
        z-index: 2000;
        overflow: auto;
        background: var(--van-background);
        color: var(--van-text-color);
      }
      .captioner-bridge[hidden] {
        display: none;
      }
      .captioner-bridge__body {
        padding: 60px 0 16px;
      }
      .captioner-bridge__status {
        padding: 16px;
        color: var(--van-text-color-2);
        text-align: center;
      }
      .captioner-bridge__control {
        width: 100%;
        color: var(--van-field-input-text-color);
        text-align: right;
        background: transparent;
        outline: none;
      }
      .captioner-bridge__button-cell {
        padding-top: 12px;
        padding-bottom: 12px;
      }
      .captioner-bridge__inline-button {
        min-width: 76px;
      }
      .captioner-bridge__hint {
        color: var(--van-text-color-2);
      }
      .captioner-bridge .van-cell__title {
        flex: .7;
      }
    `
    document.head.appendChild(style)
  }

  function ensureOverlay() {
    ensureStyle()
    let root = document.getElementById(overlayId)
    if (root) return root
    root = document.createElement('div')
    root.id = overlayId
    root.className = 'captioner-bridge'
    root.hidden = true
    document.body.appendChild(root)
    return root
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
  }

  function enumName(value, fallback) {
    if (!value) return fallback
    if (typeof value === 'string') return value
    if (typeof value === 'object' && typeof value.name === 'string') return value.name
    return fallback
  }

  function numberValue(value, fallback) {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : fallback
  }

  function targetOption() {
    const target = configs.captionerTargetLanguage || 'Chinese'
    const script = configs.captionerChineseScript || 'simplified'
    if (target === 'Chinese') {
      return script === 'traditional' ? 'ChineseTraditional' : 'ChineseSimplified'
    }
    return target
  }

  function renderSwitch(key, title) {
    const checked = Boolean(configs[key])
    return `
      <div class="van-cell van-cell--center">
        <div class="van-cell__title"><span>${escapeHtml(title)}</span></div>
        <div class="van-cell__value">
          <button type="button" class="van-switch${checked ? ' van-switch--on' : ''}" role="switch" aria-checked="${checked}" data-switch="${key}">
            <div class="van-switch__node"></div>
          </button>
        </div>
      </div>
    `
  }

  function renderInput(key, title, type) {
    const inputType = type || 'text'
    const inputMode = inputType === 'number' ? 'numeric' : ''
    return `
      <div class="van-cell van-field">
        <div class="van-cell__title van-field__label"><span>${escapeHtml(title)}</span></div>
        <div class="van-cell__value van-field__value">
          <div class="van-field__body">
            <input class="van-field__control van-field__control--right" data-input="${key}" type="${inputType}" inputmode="${inputMode}" value="${escapeHtml(configs[key] ?? '')}">
          </div>
        </div>
      </div>
    `
  }

  function renderSelect(key, title, options, value) {
    const current = value ?? configs[key]
    return `
      <div class="van-cell van-field">
        <div class="van-cell__title van-field__label"><span>${escapeHtml(title)}</span></div>
        <div class="van-cell__value van-field__value">
          <div class="van-field__body">
            <select class="captioner-bridge__control" data-select="${key}">
              ${options.map(([optionValue, text]) => `<option value="${escapeHtml(optionValue)}"${String(optionValue) === String(current) ? ' selected' : ''}>${escapeHtml(text)}</option>`).join('')}
            </select>
          </div>
        </div>
      </div>
    `
  }

  function renderModelStatus() {
    const status = modelsLoadingText()
    return `
      <div class="van-cell van-cell--center">
        <div class="van-cell__title"><span>字幕模型</span></div>
        <div class="van-cell__value">
          <span class="captioner-bridge__hint">${escapeHtml(status)}</span>
          <button type="button" class="van-button van-button--primary van-button--small captioner-bridge__inline-button" data-captioner-refresh-models${modelLoadPromise ? ' disabled' : ''}>
            <div class="van-button__content"><span class="van-button__text">${modelLoadPromise ? '获取中' : '刷新'}</span></div>
          </button>
        </div>
      </div>
    `
  }

  function renderPage(status) {
    const root = ensureOverlay()
    root.classList.toggle('van-theme-dark', document.documentElement.classList.contains('dark'))
    if (status) {
      root.innerHTML = `
        <div class="van-nav-bar van-nav-bar--fixed van-hairline--bottom">
          <div class="van-nav-bar__content">
            <div class="van-nav-bar__left" data-captioner-back><i class="van-badge__wrapper van-icon van-icon-arrow-left van-nav-bar__arrow"></i><span class="van-nav-bar__text">返回</span></div>
            <div class="van-nav-bar__title van-ellipsis">AI字幕</div>
          </div>
        </div>
        <div class="captioner-bridge__body"><div class="captioner-bridge__status">${escapeHtml(status)}</div></div>
      `
      bindBack(root)
      return
    }

    const currentModels = currentModelOptions()
    const asrModelOptions = currentModels.asrModels.map((model) => [model, model])
    const translationModelOptions = currentModels.translationModels.map((model) => [model, model])

    root.innerHTML = `
      <div class="van-nav-bar van-nav-bar--fixed van-hairline--bottom">
        <div class="van-nav-bar__content">
          <div class="van-nav-bar__left" data-captioner-back><i class="van-badge__wrapper van-icon van-icon-arrow-left van-nav-bar__arrow"></i><span class="van-nav-bar__text">返回</span></div>
          <div class="van-nav-bar__title van-ellipsis">AI字幕</div>
        </div>
      </div>
      <div class="captioner-bridge__body">
        <div class="van-cell-group van-cell-group--inset">
          ${renderSwitch('captionerEnabled', 'AI字幕')}
          ${renderInput('captionerServerUrl', '后端地址')}
          ${renderModelStatus()}
          ${renderSelect('captionerSourceLanguage', '源语言', sourceLanguages, configs.captionerSourceLanguage || 'auto')}
          ${renderSelect('captionerTargetLanguage', '目标语言', targetLanguages, targetOption())}
          ${renderSelect('captionerChineseScript', '中文书写', chineseScripts, configs.captionerChineseScript || 'simplified')}
          ${renderSwitch('captionerBilingualEnabled', '双语字幕')}
          ${renderSelect('captionerAsrModel', 'ASR模型', asrModelOptions, configs.captionerAsrModel)}
          ${renderSelect('captionerTranslationModel', '翻译模型', translationModelOptions, configs.captionerTranslationModel)}
          ${renderSelect('captionerChunkDurationMs', '分段上限', durationOptions, numberValue(configs.captionerChunkDurationMs, 5000))}
          ${renderSelect('captionerPartialBeamSize', '临时字幕搜索宽度', beamOptions, numberValue(configs.captionerPartialBeamSize, 1))}
          ${renderSelect('captionerFinalBeamSize', '修正字幕搜索宽度', beamOptions, numberValue(configs.captionerFinalBeamSize, 3))}
          ${renderSelect('captionerDisplayDurationMs', '显示时长', displayDurationOptions, numberValue(configs.captionerDisplayDurationMs, 7000))}
          ${renderSelect('captionerTextColor', '字幕颜色', textColors, enumName(configs.captionerTextColor, 'WHITE'))}
          ${renderSelect('captionerBackgroundColor', '背景颜色', backgroundColors, enumName(configs.captionerBackgroundColor, 'BLACK'))}
          ${renderSelect('captionerPosition', '显示位置', positions, enumName(configs.captionerPosition, 'BOTTOM'))}
          ${renderInput('captionerOffsetX', '水平偏移', 'number')}
          ${renderInput('captionerOffsetY', '垂直偏移', 'number')}
          ${renderSelect('captionerTextAlign', '文字对齐', textAligns, enumName(configs.captionerTextAlign, 'CENTER'))}
          ${renderSwitch('captionerSingleLineMode', '单行模式')}
          ${renderInput('captionerPrimaryFontSize', '主字幕字号', 'number')}
          ${renderInput('captionerSecondaryFontSize', '副字幕字号', 'number')}
          <div class="van-cell captioner-bridge__button-cell">
            <button type="button" class="van-button van-button--primary van-button--large van-button--block" data-captioner-save>
              <div class="van-button__content"><span class="van-button__text">更新</span></div>
            </button>
          </div>
        </div>
      </div>
    `
    bindPage(root)
  }

  function bindBack(root) {
    root.querySelector('[data-captioner-back]')?.addEventListener('click', () => {
      location.hash = '#/'
    })
  }

  function bindPage(root) {
    bindBack(root)
    root.querySelectorAll('[data-switch]').forEach((element) => {
      element.addEventListener('click', () => {
        const key = element.getAttribute('data-switch')
        configs[key] = !configs[key]
        renderPage()
      })
    })
    root.querySelectorAll('[data-input]').forEach((element) => {
      element.addEventListener('input', () => {
        const key = element.getAttribute('data-input')
        const previousValue = configs[key]
        configs[key] = element.type === 'number' ? Number(element.value || 0) : element.value
        if (key === 'captionerServerUrl' && configs[key] !== previousValue) {
          modelOptions = null
          modelLoadPromise = null
          modelLoadError = ''
          modelOptionsServerUrl = ''
          modelLoadToken += 1
        }
      })
    })
    root.querySelector('[data-input="captionerServerUrl"]')?.addEventListener('change', () => {
      loadCaptionerModels()
    })
    root.querySelectorAll('[data-select]').forEach((element) => {
      element.addEventListener('change', () => {
        const key = element.getAttribute('data-select')
        const value = element.value
        if (key === 'captionerTargetLanguage') {
          if (value === 'ChineseSimplified') {
            configs.captionerTargetLanguage = 'Chinese'
            configs.captionerChineseScript = 'simplified'
          } else if (value === 'ChineseTraditional') {
            configs.captionerTargetLanguage = 'Chinese'
            configs.captionerChineseScript = 'traditional'
          } else {
            configs.captionerTargetLanguage = value
          }
          renderPage()
          return
        }
        if (['captionerChunkDurationMs', 'captionerPartialBeamSize', 'captionerFinalBeamSize', 'captionerDisplayDurationMs'].includes(key)) {
          configs[key] = Number(value)
        } else {
          configs[key] = value
        }
      })
    })
    root.querySelector('[data-captioner-refresh-models]')?.addEventListener('click', () => {
      modelOptions = null
      modelLoadPromise = null
      modelLoadError = ''
      modelOptionsServerUrl = ''
      modelLoadToken += 1
      loadCaptionerModels()
    })
    root.querySelector('[data-captioner-save]')?.addEventListener('click', saveConfigs)
  }

  async function loadConfigs() {
    if (loading) return
    loading = true
    renderPage('加载中...')
    try {
      configs = await requestJson('/api/configs')
      fillDefaults()
      renderPage()
      loadCaptionerModels()
    } catch (error) {
      renderPage('加载失败')
      console.error(error)
    } finally {
      loading = false
    }
  }

  async function saveConfigs() {
    renderPage('更新中...')
    try {
      normalizeBeforeSave()
      await requestJson('/api/configs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(configs),
      })
      configs = await requestJson('/api/configs')
      fillDefaults()
      modelOptions = null
      modelLoadPromise = null
      modelLoadError = ''
      modelOptionsServerUrl = ''
      modelLoadToken += 1
      renderPage()
      loadCaptionerModels()
    } catch (error) {
      renderPage('更新失败')
      console.error(error)
    }
  }

  function modelsLoadingText() {
    if (modelLoadPromise) return '正在获取'
    if (modelLoadError) return modelLoadError
    if (modelOptions) return '已获取'
    return '未获取'
  }

  function currentModelOptions() {
    const options = modelOptions || { asrModels: [], translationModels: [] }
    return {
      asrModels: withCurrent(options.asrModels, configs.captionerAsrModel),
      translationModels: withCurrent(options.translationModels, configs.captionerTranslationModel),
    }
  }

  function withCurrent(values, current) {
    const result = []
    const append = (value) => {
      const text = String(value ?? '').trim()
      if (text && !result.includes(text)) result.push(text)
    }
    ;(values || []).forEach(append)
    const currentText = String(current ?? '').trim()
    if (currentText && !result.includes(currentText)) result.unshift(currentText)
    return result
  }

  async function loadCaptionerModels() {
    if (!configs) return null

    const serverUrl = String(configs.captionerServerUrl || '').trim()
    if (modelLoadPromise && serverUrl === modelOptionsServerUrl) return modelLoadPromise
    if (!serverUrl) {
      modelOptions = null
      modelLoadError = '后端地址为空'
      renderPage()
      return null
    }

    modelLoadError = ''
    modelOptionsServerUrl = serverUrl
    const token = ++modelLoadToken
    modelLoadPromise = fetchCaptionerModels(serverUrl)
      .then((options) => {
        if (token !== modelLoadToken) return
        if (String(configs.captionerServerUrl || '').trim() !== serverUrl) return
        modelOptions = options
      })
      .catch((error) => {
        if (token !== modelLoadToken) return
        if (String(configs.captionerServerUrl || '').trim() !== serverUrl) return
        modelOptions = null
        modelLoadError = '获取失败'
        console.error(error)
      })
      .finally(() => {
        if (token !== modelLoadToken) return
        if (String(configs.captionerServerUrl || '').trim() === modelOptionsServerUrl) {
          modelLoadPromise = null
          renderPage()
        }
      })

    renderPage()
    return modelLoadPromise
  }

  async function fetchCaptionerModels(serverUrl) {
    const errors = []

    try {
      const options = parseCombinedModels(await requestCaptionerJson(serverUrl, '/api/models'))
      if (!isModelOptionsEmpty(options)) return options
    } catch (error) {
      errors.push(error)
    }

    try {
      const asrModels = parseReadyModels(await requestCaptionerJson(serverUrl, '/api/models/asr'))
      const translationModels = parseReadyModels(await requestCaptionerJson(serverUrl, '/api/models/translate'))
      const options = { asrModels, translationModels }
      if (!isModelOptionsEmpty(options)) return options
    } catch (error) {
      errors.push(error)
    }

    throw errors[0] || new Error('未获取到可用模型')
  }

  async function requestCaptionerJson(serverUrl, endpoint) {
    const controller = typeof AbortController !== 'undefined' ? new AbortController() : null
    const timeout = controller ? window.setTimeout(() => controller.abort(), 10000) : null
    try {
      return await requestJson(buildCaptionerEndpointUrl(serverUrl, endpoint), {
        signal: controller ? controller.signal : undefined,
      })
    } finally {
      if (timeout) window.clearTimeout(timeout)
    }
  }

  function buildCaptionerEndpointUrl(serverUrl, endpoint) {
    const base = String(serverUrl || '').trim().replace(/\/+$/, '')
    if (!base) throw new Error('字幕后端地址为空')

    let normalizedBase = base
    if (base.startsWith('ws://')) {
      normalizedBase = `http://${base.slice(5)}`
    } else if (base.startsWith('wss://')) {
      normalizedBase = `https://${base.slice(6)}`
    } else if (!base.startsWith('http://') && !base.startsWith('https://')) {
      normalizedBase = `http://${base}`
    }

    const url = new URL(normalizedBase)
    url.pathname = endpoint
    url.search = ''
    url.hash = ''
    return url.toString()
  }

  function parseJsonPayload(payload) {
    if (typeof payload === 'string') return JSON.parse(payload)
    return payload
  }

  function parseCombinedModels(payload) {
    const root = parseJsonPayload(payload)
    if (!root || typeof root !== 'object' || Array.isArray(root)) {
      return { asrModels: [], translationModels: [] }
    }
    return parseCombinedObject(root)
  }

  function parseCombinedObject(root) {
    if (root.data && typeof root.data === 'object' && !Array.isArray(root.data)) {
      return parseCombinedObject(root.data)
    }

    const readyAsrModels = toModelNames(root.readyAsrModels, false)
    const readyTranslationModels = toModelNames(root.readyTranslationModels, false)
    const asrModels = readyAsrModels.length ? readyAsrModels : toModelNames(root.asrModels, true)
    const translationModels = readyTranslationModels.length
      ? readyTranslationModels
      : toModelNames(root.translationModels, true)

    return {
      asrModels: distinctModels(asrModels),
      translationModels: distinctModels(translationModels),
    }
  }

  function parseReadyModels(payload) {
    const root = parseJsonPayload(payload)
    if (Array.isArray(root)) return toModelNames(root, true)
    if (!root || typeof root !== 'object') return []
    if (Array.isArray(root.data)) return toModelNames(root.data, true)
    return toModelNames(root.models, true)
  }

  function toModelNames(values, readyOnly) {
    if (!Array.isArray(values)) return []
    return distinctModels(values.map((item) => {
      if (typeof item === 'string') return item
      if (!item || typeof item !== 'object') return ''
      if (readyOnly && !isReadyModel(item)) return ''
      return item.key || item.name || item.id || item.model || item.value || ''
    }))
  }

  function isReadyModel(model) {
    return model.ready === true || String(model.ready).toLowerCase() === 'true'
  }

  function distinctModels(values) {
    const result = []
    ;(values || []).forEach((value) => {
      const text = String(value || '').trim()
      if (text && !result.includes(text)) result.push(text)
    })
    return result
  }

  function isModelOptionsEmpty(options) {
    return !options.asrModels.length && !options.translationModels.length
  }

  async function requestJson(url, options) {
    const response = await fetch(url, options)
    if (!response.ok) throw new Error(`${response.status} ${response.statusText}`)
    const contentType = response.headers.get('Content-Type') || ''
    if (contentType.includes('application/json')) return response.json()
    return response.text()
  }

  function fillDefaults() {
    if (configs.captionerEnabled == null) configs.captionerEnabled = false
    if (configs.captionerServerUrl == null) configs.captionerServerUrl = 'http://127.0.0.1:8765'
    if (configs.captionerSourceLanguage == null) configs.captionerSourceLanguage = 'auto'
    if (configs.captionerTargetLanguage == null) configs.captionerTargetLanguage = 'Chinese'
    if (configs.captionerChineseScript == null) configs.captionerChineseScript = 'simplified'
    if (configs.captionerBilingualEnabled == null) configs.captionerBilingualEnabled = true
    if (configs.captionerAsrModel == null) configs.captionerAsrModel = 'large-v2'
    if (configs.captionerTranslationModel == null) configs.captionerTranslationModel = 'qwen2.5-1.5b-instruct-gguf'
    if (configs.captionerChunkDurationMs == null) configs.captionerChunkDurationMs = 5000
    if (configs.captionerPartialBeamSize == null) configs.captionerPartialBeamSize = 1
    if (configs.captionerFinalBeamSize == null) configs.captionerFinalBeamSize = 3
    if (configs.captionerDisplayDurationMs == null) configs.captionerDisplayDurationMs = 7000
    configs.captionerTextColor = enumName(configs.captionerTextColor, 'WHITE')
    configs.captionerBackgroundColor = enumName(configs.captionerBackgroundColor, 'BLACK')
    configs.captionerPosition = enumName(configs.captionerPosition, 'BOTTOM')
    if (configs.captionerOffsetX == null) configs.captionerOffsetX = 0
    if (configs.captionerOffsetY == null) configs.captionerOffsetY = 0
    configs.captionerTextAlign = enumName(configs.captionerTextAlign, 'CENTER')
    if (configs.captionerSingleLineMode == null) configs.captionerSingleLineMode = false
    if (configs.captionerPrimaryFontSize == null) configs.captionerPrimaryFontSize = 26
    if (configs.captionerSecondaryFontSize == null) configs.captionerSecondaryFontSize = 18
  }

  function normalizeBeforeSave() {
    configs.captionerServerUrl = String(configs.captionerServerUrl || '').trim()
    configs.captionerOffsetX = numberValue(configs.captionerOffsetX, 0)
    configs.captionerOffsetY = numberValue(configs.captionerOffsetY, 0)
    configs.captionerPrimaryFontSize = numberValue(configs.captionerPrimaryFontSize, 26)
    configs.captionerSecondaryFontSize = numberValue(configs.captionerSecondaryFontSize, 18)
  }

  function isCaptionerRoute() {
    return location.hash.replace(/\/$/, '') === CAPTIONER_HASH
  }

  function renderRoute() {
    const root = ensureOverlay()
    const app = document.getElementById('app')
    if (isCaptionerRoute()) {
      if (app) app.style.display = 'none'
      root.hidden = false
      if (!configs) loadConfigs()
      else renderPage()
    } else {
      root.hidden = true
      if (app) app.style.display = ''
      insertMenuEntry()
    }
  }

  function insertMenuEntry() {
    if (document.querySelector('[data-captioner-entry]')) return
    const cells = Array.from(document.querySelectorAll('#app .van-cell'))
    const playerCell = cells.find((cell) => (cell.querySelector('.van-cell__title')?.textContent || '').trim() === '播放器')
    if (!playerCell) return
    const entry = document.createElement('div')
    entry.className = 'van-cell van-cell--clickable'
    entry.setAttribute('role', 'button')
    entry.setAttribute('tabindex', '0')
    entry.setAttribute('data-captioner-entry', '')
    entry.innerHTML = '<div class="van-cell__title"><span>AI字幕</span></div><i class="van-badge__wrapper van-icon van-icon-arrow van-cell__right-icon"></i>'
    const open = () => {
      location.hash = CAPTIONER_HASH
    }
    entry.addEventListener('click', open)
    entry.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        open()
      }
    })
    playerCell.after(entry)
  }

  function boot() {
    ensureOverlay()
    renderRoute()
    window.addEventListener('hashchange', renderRoute)
    const observer = new MutationObserver(() => {
      if (!isCaptionerRoute()) insertMenuEntry()
    })
    observer.observe(document.body, { childList: true, subtree: true })
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot)
  } else {
    boot()
  }
})()
