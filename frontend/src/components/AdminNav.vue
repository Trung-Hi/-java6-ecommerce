<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from "vue";
import {useRouter, useRoute} from "vue-router";
import {api} from "@/api";
import {useSession} from "@/composables/useSession";

const props = defineProps({
    isCollapsed: { type: Boolean, default: false }
});

const emit = defineEmits(['update:isCollapsed', 'close']);

const route = useRoute();

const navItems = [
    { name: 'Danh mục', path: '/admin/category', icon: 'LayoutGrid' },
    { name: 'Sản phẩm', path: '/admin/product', icon: 'Package' },
    { name: 'Tài khoản', path: '/admin/account', icon: 'Users' },
    { name: 'Đơn hàng', path: '/admin/order', icon: 'ClipboardList' },
    { name: 'Doanh thu', path: '/admin/revenue', icon: 'BarChart3' },
    { name: 'Khách VIP', path: '/admin/vip', icon: 'Crown' },
    { name: 'Chat hỗ trợ', path: '/admin/chat', icon: 'MessageCircle' },
];

const isActive = (path) => route.path === path;

const handleNavClick = () => {
    // Emit close event for mobile sidebar
    emit('close');
};
</script>

<template>
    <nav class="admin-nav" :class="{ 'collapsed': isCollapsed }">
        <router-link
            v-for="item in navItems"
            :key="item.path"
            class="admin-nav-link"
            :class="{ 'active': isActive(item.path) }"
            :to="item.path"
            :title="isCollapsed ? item.name : ''"
            @click="handleNavClick"
        >
            <div class="icon-wrap">
                <!-- Fallback to simple SVG since we can't easily dynamic import Lucide in this context without complex setup -->
                <component :is="item.icon === 'LayoutGrid' ? 'svg' : 'svg'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <template v-if="item.icon === 'LayoutGrid'">
                        <rect x="3" y="3" width="7" height="7"></rect>
                        <rect x="14" y="3" width="7" height="7"></rect>
                        <rect x="14" y="14" width="7" height="7"></rect>
                        <rect x="3" y="14" width="7" height="7"></rect>
                    </template>
                    <template v-else-if="item.icon === 'Package'">
                        <path d="m7.5 4.27 9 5.15"></path>
                        <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"></path>
                        <path d="m3.3 7 8.7 5 8.7-5"></path>
                        <path d="M12 22V12"></path>
                    </template>
                    <template v-else-if="item.icon === 'Users'">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </template>
                    <template v-else-if="item.icon === 'ClipboardList'">
                        <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
                        <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
                        <path d="M12 11h4"></path>
                        <path d="M12 16h4"></path>
                        <path d="M8 11h.01"></path>
                        <path d="M8 16h.01"></path>
                    </template>
                    <template v-else-if="item.icon === 'BarChart3'">
                        <path d="M3 3v18h18"></path>
                        <path d="M18 17V9"></path>
                        <path d="M13 17V5"></path>
                        <path d="M8 17v-3"></path>
                    </template>
                    <template v-else-if="item.icon === 'Crown'">
                        <path d="m2 4 3 12h14l3-12-6 7-4-7-4 7-6-7Z"></path>
                    </template>
                    <template v-else-if="item.icon === 'MessageCircle'">
                        <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"></path>
                    </template>
                </component>
            </div>
            <span v-if="!isCollapsed" class="nav-text">{{ item.name }}</span>
        </router-link>
    </nav>
</template>

<style scoped>
.admin-nav {
    display: flex;
    flex-direction: column;
    gap: 0;
    padding: 20px 0 0 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.admin-nav-link {
    display: flex;
    align-items: center;
    padding: 14px 24px;
    border-radius: 0;
    color: #666666;
    text-decoration: none;
    transition: all 0.2s ease;
    position: relative;
    border-left: 3px solid transparent;
}

.icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    flex-shrink: 0;
}

.nav-text {
    margin-left: 16px;
    font-size: 0.9rem;
    font-weight: 400;
    white-space: nowrap;
}

.admin-nav-link:hover {
    background: #f5f5f5;
    color: #000000;
}

.admin-nav-link.active {
    background: #f5f5f5;
    color: #000000;
    border-left-color: #000000;
}

.collapsed .admin-nav-link {
    padding: 12px 0;
    justify-content: center;
}

@media (max-width: 1024px) {
    .admin-nav {
        flex-direction: row;
        overflow-x: auto;
        padding: 8px;
        gap: 4px;
    }
    
    .admin-nav-link {
        flex-shrink: 0;
        min-width: auto;
        padding: 10px 16px;
        border-left: none;
        border-bottom: 2px solid transparent;
    }
    
    .admin-nav-link.active {
        border-left-color: transparent;
        border-bottom-color: #000000;
        background: transparent;
    }
}
</style>
