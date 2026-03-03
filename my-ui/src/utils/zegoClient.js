import { ZegoExpressEngine } from 'zego-express-engine-webrtc'

class ZegoClient {
  constructor() {
    this.zg = null
    this.appID = 1556046237 // Replace with your AppID
    this.server = 'wss://accesshub-wss.zego.im/accesshub' // Replace with your Server URL
    this.isInitialized = false
    this.localStream = null
  }

  // Initialize the SDK
  async init(appID, server) {
    if (this.isInitialized) return

    if (appID) this.appID = appID
    if (server) this.server = server
    
    if (!this.appID || !this.server) {
      console.error('[ZegoClient] Missing AppID or Server URL')
      return
    }

    this.zg = new ZegoExpressEngine(this.appID, this.server)
    this.isInitialized = true
    console.log('[ZegoClient] Initialized')
    
    // Set up event listeners
    this.zg.on('roomStateUpdate', (roomID, state, errorCode, extendedData) => {
      console.log(`[ZegoClient] roomStateUpdate: ${state}, errorCode: ${errorCode}`)
    })
    
    this.zg.on('publisherStateUpdate', (result) => {
      console.log(`[ZegoClient] publisherStateUpdate: ${result.state}`)
    })
  }

  // Check browser compatibility
  async checkSystemRequirements() {
    const result = await this.zg.checkSystemRequirements()
    console.log('[ZegoClient] System requirements:', result)
    return result
  }

  // Login to a room
  async loginRoom(roomID, token, user) {
    if (!this.isInitialized) throw new Error('SDK not initialized')

    try {
      const result = await this.zg.loginRoom(roomID, token, user, { userUpdate: true })
      console.log('[ZegoClient] Login success:', result)
      return true
    } catch (error) {
      console.error('[ZegoClient] Login failed:', error)
      throw error
    }
  }

  // Start publishing audio stream (Voice Reply)
  async startVoiceReply(streamID) {
    if (!this.isInitialized) throw new Error('SDK not initialized')

    try {
      // Create stream (audio only for voice reply, or video if needed)
      // camera: { video: false, audio: true } for audio only
      this.localStream = await this.zg.createStream({ camera: { video: false, audio: true } })
      
      // Start publishing
      const result = this.zg.startPublishingStream(streamID, this.localStream)
      console.log('[ZegoClient] Start publishing stream:', streamID)
      return true
    } catch (error) {
      console.error('[ZegoClient] Start voice reply failed:', error)
      throw error
    }
  }

  // Stop publishing
  stopVoiceReply(streamID) {
    if (this.localStream) {
      this.zg.destroyStream(this.localStream)
      this.localStream = null
    }
    this.zg.stopPublishingStream(streamID)
    console.log('[ZegoClient] Stop publishing stream:', streamID)
  }

  // Logout
  logoutRoom(roomID) {
    this.zg.logoutRoom(roomID)
  }
}

export default new ZegoClient()
