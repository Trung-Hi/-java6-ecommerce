import {createApp} from "vue";
import App from "@/App.vue";
import router from "@/router";
import "@/styles.css";

const app = createApp(App);

// Auto-adjust product grids based on count
app.directive('auto-grid', {
    mounted(el) {
        const observer = new MutationObserver(() => {
            const count = el.querySelectorAll('.product-card').length;
            el.classList.remove('grid-2', 'grid-3', 'grid-4', 'grid-5', 'grid-6', 'grid-7', 'grid-8');
            if (count >= 2 && count <= 8) {
                el.classList.add(`grid-${count}`);
            }
        });
        
        observer.observe(el, { childList: true, subtree: true });
        
        // Initial check
        const count = el.querySelectorAll('.product-card').length;
        if (count >= 2 && count <= 8) {
            el.classList.add(`grid-${count}`);
        }
    }
});

app.use(router).mount("#app");
