<template>
  <div class="home-page">
    <div class="content-container">
      <h1>欢迎来到编程导航</h1>
      <p>这是一个专为程序员打造的学习资源导航平台</p>
      
      <div class="feature-grid">
        <div class="feature-card">
          <h3>📚 学习资源</h3>
          <p>精选优质的编程学习资源和教程</p>
        </div>
        
        <div class="feature-card">
          <h3>🛠️ 开发工具</h3>
          <p>实用的编程工具和开发环境</p>
        </div>
        
        <div class="feature-card">
          <h3>👥 社区交流</h3>
          <p>与其他程序员交流学习心得</p>
        </div>
        
        <div class="feature-card">
          <h3>📖 技术文档</h3>
          <p>详细的技术文档和 API 参考</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.home-page {
  padding-top: 64px; /* 为固定导航栏留出空间 */
  min-height: calc(100vh - 64px - 80px); /* 减去导航栏和页脚的高度 */
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
}

h1 {
  font-size: 2.5rem;
  color: #1890ff;
  text-align: center;
  margin-bottom: 16px;
}

p {
  font-size: 1.1rem;
  color: #666;
  text-align: center;
  margin-bottom: 48px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-top: 48px;
}

.feature-card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.feature-card h3 {
  font-size: 1.2rem;
  color: #1890ff;
  margin-bottom: 12px;
}

.feature-card p {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-container {
    padding: 32px 16px;
  }
  
  h1 {
    font-size: 2rem;
  }
  
  .feature-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>

