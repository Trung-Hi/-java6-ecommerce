import {createApp, computed} from "https://unpkg.com/vue@3/dist/vue.esm-browser.js";
import {createRouter, createWebHashHistory} from "https://unpkg.com/vue-router@4/dist/vue-router.esm-browser.js";
import {routes} from "/vue/pages.js";

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

const App = {
    setup() {
        const menu = computed(() => routes.filter(route => route.meta?.title));
        const groupedMenu = computed(() => {
            const groups = {web: [], account: [], order: [], admin: []};
            menu.value.forEach(item => {
                if (item.path.startsWith("/admin")) {
                    groups.admin.push(item);
                } else if (item.path.startsWith("/account")) {
                    groups.account.push(item);
                } else if (item.path.startsWith("/order")) {
                    groups.order.push(item);
                } else {
                    groups.web.push(item);
                }
            });
            return groups;
        });
        return {groupedMenu};
    },
    template: `
        <div class="container-fluid">
            <div class="row min-vh-100">
                <aside class="col-md-3 col-lg-2 sidebar py-3">
                    <h5 class="px-3 mb-3">Vue API Navigation</h5>
                    <div class="nav-group">
                        <h6 class="px-3 text-muted">Web</h6>
                        <router-link v-for="item in groupedMenu.web" :key="item.path" :to="item.path" class="nav-link">{{ item.meta.title }}</router-link>
                    </div>
                    <div class="nav-group">
                        <h6 class="px-3 text-muted">Account</h6>
                        <router-link v-for="item in groupedMenu.account" :key="item.path" :to="item.path" class="nav-link">{{ item.meta.title }}</router-link>
                    </div>
                    <div class="nav-group">
                        <h6 class="px-3 text-muted">Order</h6>
                        <router-link v-for="item in groupedMenu.order" :key="item.path" :to="item.path" class="nav-link">{{ item.meta.title }}</router-link>
                    </div>
                    <div class="nav-group">
                        <h6 class="px-3 text-muted">Admin</h6>
                        <router-link v-for="item in groupedMenu.admin" :key="item.path" :to="item.path" class="nav-link">{{ item.meta.title }}</router-link>
                    </div>
                </aside>
                <main class="col-md-9 col-lg-10 py-4">
                    <router-view />
                </main>
            </div>
        </div>
    `
};

createApp(App).use(router).mount("#app");
