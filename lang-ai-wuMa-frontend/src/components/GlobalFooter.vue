<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <p class="copyright">
        编程导航原创项目 by 
        <a 
          href="https://www.codefather.cn" 
          target="_blank" 
          rel="noopener noreferrer"
          class="author-link"
        >
          程序员鱼皮
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.global-footer {
  background: #001529;
  color: #fff;
  text-align: center;
  padding: 24px 0;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.copyright {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.author-link {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.author-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 0 16px;
  }
  
  .copyright {
    font-size: 12px;
  }
}
</style>

