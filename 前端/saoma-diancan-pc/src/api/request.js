import axios from 'axios'

class Request {
  constructor(url, data) {
    this.url = url
    this.data = data || {}
  }

  // POST请求
  async modepost() {
    try {
      const response = await axios.post(this.url, this.data, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
      return response.data
    } catch (error) {
      console.error('POST请求错误:', error)
      throw error
    }
  }

  // GET请求
  async modeget() {
    try {
      const response = await axios.get(this.url, {
        params: this.data
      })
      return response.data
    } catch (error) {
      console.error('GET请求错误:', error)
      throw error
    }
  }
}

export default Request