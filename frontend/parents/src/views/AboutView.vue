<template>
  <div class="about-container">
    <div class="about-header">
      <h1>关于养老系统</h1>
      <p>关爱老人，服务社会</p>
    </div>
    
    <div class="about-content">
      <div class="info-section">
        <h3>系统信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">系统版本</span>
            <span class="value">v1.0.0</span>
          </div>
          <div class="info-item">
            <span class="label">开发团队</span>
            <span class="value">养老科技团队</span>
          </div>
          <div class="info-item">
            <span class="label">联系方式</span>
            <span class="value">400-123-4567</span>
          </div>
          <div class="info-item">
            <span class="label">服务时间</span>
            <span class="value">24小时全天候</span>
          </div>
        </div>
      </div>
      
      <div class="features-section">
        <h3>主要功能</h3>
        <div class="features-grid">
          <div class="feature-item">
            <el-icon size="32" color="#409eff"><UserFilled /></el-icon>
            <h4>刷脸登录</h4>
            <p>便捷安全的身份验证方式</p>
          </div>
          <div class="feature-item">
            <el-icon size="32" color="#67c23a"><ShoppingCart /></el-icon>
            <h4>代购服务</h4>
            <p>为老人提供生活用品代购</p>
          </div>
          <div class="feature-item">
            <el-icon size="32" color="#f56c6c"><Warning /></el-icon>
            <h4>紧急呼叫</h4>
            <p>一键求助，快速响应</p>
          </div>
          <div class="feature-item">
            <el-icon size="32" color="#e6a23c"><ChatDotRound /></el-icon>
            <h4>AI助手</h4>
            <p>智能问答，贴心陪伴</p>
          </div>
        </div>
      </div>
      
      <div class="contact-section">
        <h3>联系我们</h3>
        <div class="contact-info">
          <div class="contact-item">
            <el-icon><Phone /></el-icon>
            <span>客服热线：400-123-4567</span>
          </div>
          <div class="contact-item">
            <el-icon><Message /></el-icon>
            <span>邮箱：support@elderly-care.com</span>
          </div>
          <div class="contact-item">
            <el-icon><Location /></el-icon>
            <span>地址：北京市朝阳区养老科技园</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Phone, Message, Location } from '@element-plus/icons-vue'
</script>

<style scoped>
.about-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}

.about-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
}

.about-header h1 {
  margin: 0 0 10px 0;
  font-size: 32px;
  font-weight: 600;
}

.about-header p {
  margin: 0;
  font-size: 18px;
  opacity: 0.9;
}

.about-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.info-section, .features-section, .contact-section {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.info-section h3, .features-section h3, .contact-section h3 {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.info-item .label {
  font-size: 16px;
  color: #666;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-item {
  text-align: center;
  padding: 25px 20px;
  background: #f8f9fa;
  border-radius: 15px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.feature-item h4 {
  margin: 15px 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.feature-item p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 10px;
  font-size: 16px;
  color: #333;
}

.contact-item .el-icon {
  color: #409eff;
  font-size: 20px;
}

/* 老人端响应式优化 */
@media (max-width: 768px) {
  .about-container {
    padding: 15px;
  }
  
  .about-header {
    padding: 30px 20px;
  }
  
  .about-header h1 {
    font-size: 28px;
  }
  
  .about-header p {
    font-size: 16px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .info-section, .features-section, .contact-section {
    padding: 20px;
  }
  
  .feature-item {
    padding: 20px 15px;
  }
  
  .feature-item h4 {
    font-size: 16px;
  }
  
  .contact-item {
    padding: 12px 15px;
    font-size: 14px;
  }
}
</style>
