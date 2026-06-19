import { createRouter, createWebHistory } from 'vue-router'
import { isMobile } from '@/utils/device'

const routes = [
  {
    path: '/',
    redirect: () => {
      return isMobile() ? '/h5/login' : '/pc/login'
    }
  },
  {
    path: '/pc/login',
    name: 'PcLogin',
    component: () => import('@/views/pc/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/h5/login',
    name: 'H5Login',
    component: () => import('@/views/h5/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/pc',
    component: () => import('@/layouts/PcLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/pc/schedule'
      },
      {
        path: 'file',
        component: () => import('@/views/pc/File.vue')
      },
      {
        path: 'work',
        component: () => import('@/views/pc/Work.vue')
      },
      {
        path: 'schedule',
        component: () => import('@/views/pc/Schedule.vue')
      },
      {
        path: 'supply-record',
        component: () => import('@/views/pc/SupplyRecord.vue')
      },
      {
        path: 'work-hour',
        component: () => import('@/views/pc/WorkHour.vue')
      },
      {
        path: 'user',
        component: () => import('@/views/pc/User.vue')
      }
    ]
  },
  {
    path: '/h5',
    component: () => import('@/layouts/H5Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/h5/schedule'
      },
      {
        path: 'file',
        component: () => import('@/views/h5/File.vue')
      },
      {
        path: 'work',
        component: () => import('@/views/h5/Work.vue')
      },
      {
        path: 'schedule',
        component: () => import('@/views/h5/Schedule.vue')
      },
      {
        path: 'supply-record',
        component: () => import('@/views/h5/SupplyRecord.vue')
      },
      {
        path: 'user',
        component: () => import('@/views/h5/User.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  const isLoggedIn = !!userStr

  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
    && to.name !== 'PcLogin'
    && to.name !== 'H5Login'

  if (requiresAuth && !isLoggedIn) {
    if (to.path.startsWith('/h5')) {
      next({ path: '/h5/login' })
    } else {
      next({ path: '/pc/login' })
    }
  } else if ((to.name === 'PcLogin' || to.name === 'H5Login') && isLoggedIn) {
    if (to.name === 'PcLogin') {
      next({ path: '/pc/schedule' })
    } else {
      next({ path: '/h5/schedule' })
    }
  } else {
    next()
  }
})

export default router
