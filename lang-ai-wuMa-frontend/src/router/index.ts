import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import HomePage from "@/pages/HomePage.vue";
import UserRegisterPage from "@/pages/user/UserRegisterPage.vue";
import UserLoginPage from "@/pages/user/UserLoginPage.vue";
import UserManagePage from "@/pages/admin/UserManagePage.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage
    },
    {
      path: '/user/register',
      name: '用户注册',
      component:UserRegisterPage
    },
    {
      path: '/user/login',
      name: '用户登录',
      component:UserLoginPage
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component:UserManagePage
    }
  ],
})

export default router
