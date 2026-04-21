<script setup>
import {ref} from "vue";
import AdminNav from "./AdminNav.vue";

const sidebarOpen = ref(false);

const toggleSidebar = () => {
    sidebarOpen.value = !sidebarOpen.value;
};

const closeSidebar = () => {
    sidebarOpen.value = false;
};
</script>

<template>
    <div class="adminLayout">
        <!-- Mobile Header with Hamburger -->
        <div class="mobileHeader">
            <button class="hamburgerBtn" type="button" @click="toggleSidebar" :class="{ active: sidebarOpen }">
                <span></span>
                <span></span>
                <span></span>
            </button>
        </div>

        <!-- Sidebar Overlay for Mobile -->
        <div class="sidebarOverlay" :class="{ open: sidebarOpen }" @click="closeSidebar"></div>

        <!-- Main Layout -->
        <div class="adminLayoutBody">
            <!-- Sidebar -->
            <aside class="adminSidebar" :class="{ mobileOpen: sidebarOpen }">
                <div class="adminSidebarInner">
                    <AdminNav @close="closeSidebar" />
                </div>
            </aside>
            
            <!-- Content Area -->
            <main class="adminContent">
                <slot></slot>
            </main>
        </div>
    </div>
</template>

<style scoped>
/* ==================== ADMIN LAYOUT ==================== */
.adminLayout {
    min-height: 100vh;
    display: flex;
    background: #f8f8f8;
}

/* ==================== MOBILE HEADER ==================== */
.mobileHeader {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    background: #ffffff;
    padding: 12px 16px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
    border-bottom: 1px solid #e5e5e5;
}

.hamburgerBtn {
    display: flex;
    flex-direction: column;
    gap: 5px;
    background: none;
    border: none;
    cursor: pointer;
    padding: 4px;
    width: 32px;
    height: 32px;
    justify-content: center;
    align-items: center;
}

.hamburgerBtn span {
    display: block;
    width: 24px;
    height: 2px;
    background: #333333;
    border-radius: 2px;
    transition: all 0.3s ease;
}

.hamburgerBtn.active span:nth-child(1) {
    transform: rotate(45deg) translate(5px, 5px);
}

.hamburgerBtn.active span:nth-child(2) {
    opacity: 0;
}

.hamburgerBtn.active span:nth-child(3) {
    transform: rotate(-45deg) translate(5px, -5px);
}

/* ==================== SIDEBAR OVERLAY ==================== */
.sidebarOverlay {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.sidebarOverlay.open {
    display: block;
    opacity: 1;
}

/* ==================== LAYOUT BODY ==================== */
.adminLayoutBody {
    display: flex;
    flex: 1;
    max-width: 1600px;
    margin: 0 auto;
    width: 100%;
    padding: 24px;
    gap: 24px;
}

/* ==================== SIDEBAR ==================== */
.adminSidebar {
    flex-shrink: 0;
    width: 280px;
    transition: transform 0.3s ease;
}

.adminSidebarInner {
    background: #ffffff;
    border-radius: 0;
    box-shadow: 1px 0 10px rgba(0, 0, 0, 0.05);
    border: none;
    border-right: 1px solid #e5e5e5;
    padding: 0;
    position: sticky;
    top: 0;
    min-height: 100vh;
}

/* ==================== CONTENT AREA ==================== */
.adminContent {
    flex: 1;
    min-width: 0;
    background: transparent;
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 1024px) {
    .adminLayoutBody {
        flex-direction: column;
    }
    
    .adminSidebar {
        width: 100%;
    }
    
    .adminSidebarInner {
        position: static;
    }
}

@media (max-width: 768px) {
    .mobileHeader {
        display: block;
    }
    
    .sidebarOverlay {
        display: block;
    }
    
    .adminLayoutBody {
        padding: 16px;
        padding-top: 64px; /* Space for mobile header */
    }
    
    .adminSidebar {
        position: fixed;
        top: 0;
        left: 0;
        width: 280px;
        height: 100vh;
        z-index: 1000;
        transform: translateX(-100%);
        margin: 0;
        padding-top: 56px; /* Space for mobile header */
    }
    
    .adminSidebar.mobileOpen {
        transform: translateX(0);
    }
    
    .adminSidebarInner {
        border-radius: 0;
        box-shadow: none;
        border: none;
        border-right: 1px solid #e2e8f0;
        min-height: 100vh;
        padding: 12px;
    }
    
    .adminContent {
        width: 100%;
    }
}

@media (max-width: 480px) {
    .mobileHeader {
        padding: 10px 12px;
    }
    
    .adminLayoutBody {
        padding: 12px;
        padding-top: 60px;
    }
    
    .adminSidebar {
        width: 260px;
    }
    
    .adminSidebarInner {
        padding: 10px;
    }
}
</style>
