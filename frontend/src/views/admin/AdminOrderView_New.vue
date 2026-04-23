<script setup>
import { computed, ref, watch } from "vue";
import { AdminOrderPage } from "@/legacy/pages";
import AdminLayout from "@/components/AdminLayout.vue";
import { 
  Search, Filter, RotateCcw, Eye, Trash2, ChevronLeft, ChevronRight,
  Clock, Package, Truck, CheckCircle2, AlertCircle, X, MapPin, 
  CreditCard, User, Calendar, Receipt, ExternalLink, ShoppingCart, Mail, Phone
} from 'lucide-vue-next'

const { rows, selected, msg, paging, load, detail: fetchDetail, remove } = AdminOrderPage.setup();

// ==================== STATUS HELPERS ====================
const statusMap = {
    PENDING_PAYMENT: { label: "Chờ thanh toán", color: "badge-warning", icon: Clock },
    PLACED_UNPAID: { label: "Đã đặt (COD)", color: "badge-info", icon: Package },
    PLACED_PAID: { label: "Đã đặt (Đã TT)", color: "badge-info", icon: Package },
    SHIPPING_UNPAID: { label: "Đang giao (COD)", color: "badge-info", icon: Truck },
    SHIPPING_PAID: { label: "Đang giao (Đã TT)", color: "badge-info", icon: Truck },
    DELIVERED_SUCCESS: { label: "Đã giao", color: "badge-success", icon: CheckCircle2 },
    DELIVERY_FAILED: { label: "Giao thất bại", color: "badge-danger", icon: X },
    REFUND_REQUEST: { label: "Hoàn tiền", color: "badge-danger", icon: AlertCircle },
    CANCEL: { label: "Đã hủy", color: "badge-muted", icon: X }
};

const getStatusInfo = (status) => statusMap[status] || { label: status, color: "badge-muted", icon: AlertCircle };

// ==================== FILTER STATE ====================
const searchQuery = ref("");
const debouncedSearchQuery = ref("");
const filterStatus = ref("");
let searchTimeout = null;

watch(searchQuery, (newVal) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        debouncedSearchQuery.value = newVal;
    }, 300);
});

const filteredRows = computed(() => {
    let result = rows.value || [];
    if (debouncedSearchQuery.value.trim()) {
        const query = debouncedSearchQuery.value.toLowerCase().trim();
        result = result.filter(o =>
            String(o.id).includes(query) ||
            (o.account?.username?.toLowerCase().includes(query)) ||
            (o.account?.fullname?.toLowerCase().includes(query)) ||
            (o.address?.toLowerCase().includes(query))
        );
    }
    if (filterStatus.value) {
        result = result.filter(o => o.status === filterStatus.value);
    }
    return result;
});

const totalItems = computed(() => filteredRows.value.length);
const itemsPerPage = 10;
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage));
const currentPage = ref(1);

const paginatedOrders = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return filteredRows.value.slice(start, end);
});

const resetFilters = () => {
    searchQuery.value = "";
    filterStatus.value = "";
    currentPage.value = 1;
};

const hasActiveFilters = computed(() => searchQuery.value || filterStatus.value);

// ==================== DETAIL PANEL ====================
const isPanelOpen = ref(false);

const openDetail = async (id) => {
    await fetchDetail(id);
    isPanelOpen.value = true;
};

const closePanel = () => {
    isPanelOpen.value = false;
};

const formatMoney = (value) => Number(value || 0).toLocaleString("vi-VN") + " đ";

// ==================== STATUS TABS ====================
const statusTabs = [
    { value: "", label: "Tất cả" },
    { value: "PENDING_PAYMENT", label: "Chờ thanh toán" },
    { value: "PLACED_PAID", label: "Đã đặt" },
    { value: "SHIPPING_PAID", label: "Đang giao" },
    { value: "DELIVERED_SUCCESS", label: "Đã giao" },
    { value: "REFUND_REQUEST", label: "Hoàn tiền", isAlert: true }
];
</script>

<template>
  <AdminLayout>
    <div class="space-y-24">
      <!-- Tabs & Search -->
      <div class="flex flex-col gap-20">
        <div class="flex items-center justify-between border-b border-border">
          <div class="flex gap-24">
            <button 
              v-for="tab in statusTabs" 
              :key="tab.value"
              @click="filterStatus = tab.value; currentPage = 1"
              class="pb-12 text-[14px] font-medium transition-all relative"
              :class="[
                filterStatus === tab.value ? 'text-primary' : 'text-secondary hover:text-primary',
                tab.isAlert && filteredRows.filter(o => o.status === 'REFUND_REQUEST').length > 0 ? 'pr-12' : ''
              ]"
            >
              {{ tab.label }}
              <div v-if="filterStatus === tab.value" class="absolute bottom-0 left-0 right-0 h-2 bg-primary"></div>
              <span v-if="tab.isAlert && filteredRows.filter(o => o.status === 'REFUND_REQUEST').length > 0" 
                class="absolute top-0 -right-4 w-6 h-6 bg-danger rounded-full border-2 border-white"></span>
            </button>
          </div>
          
          <div class="relative w-[300px] mb-8">
            <Search class="absolute left-12 top-1/2 -translate-y-1/2 text-secondary" :size="16" />
            <input v-model="searchQuery" type="text" class="input pl-40 h-36" placeholder="Mã đơn, khách hàng...">
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-container shadow-sm border-border/60">
        <table class="table">
          <thead class="table-header">
            <tr>
              <th class="w-[100px]">Đơn hàng</th>
              <th>Khách hàng</th>
              <th>Ngày đặt</th>
              <th class="text-right">Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Địa chỉ giao hàng</th>
              <th class="w-[80px] text-right"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in paginatedOrders" :key="order.id" class="table-row group">
              <td class="table-cell">
                <span class="text-mono font-bold text-primary">#{{ order.id }}</span>
              </td>
              <td class="table-cell">
                <div class="flex flex-col">
                  <span class="font-bold text-primary">{{ order.account?.fullname || 'Ẩn danh' }}</span>
                  <span class="text-[12px] text-secondary">@{{ order.account?.username }}</span>
                </div>
              </td>
              <td class="table-cell text-secondary text-[13px]">
                 <div class="flex items-center gap-6">
                    <Calendar :size="12" />
                    {{ order.createdAt || '20/03/2024' }}
                 </div>
              </td>
              <td class="table-cell text-right font-bold text-primary">
                {{ formatMoney(order.finalTotal) }}
              </td>
              <td class="table-cell">
                <span class="badge" :class="getStatusInfo(order.status).color">
                  {{ getStatusInfo(order.status).label }}
                </span>
              </td>
              <td class="table-cell max-w-[200px]">
                <p class="text-[13px] text-secondary truncate" :title="order.address">{{ order.address }}</p>
              </td>
              <td class="table-cell">
                <div class="flex justify-end">
                  <button @click="openDetail(order.id)" class="btn-ghost p-8 rounded-md hover:bg-surface">
                    <Eye :size="18" />
                  </button>
                </div>
              </td>
            </tr>

            <!-- Empty State -->
            <tr v-if="filteredRows.length === 0">
              <td colspan="7" class="py-80 text-center">
                <div class="flex flex-col items-center gap-16">
                  <div class="w-64 h-64 bg-surface rounded-full flex items-center justify-center text-secondary/30">
                    <ShoppingCart :size="32" />
                  </div>
                  <p class="font-bold text-primary text-lg">Không tìm thấy đơn hàng</p>
                  <button @click="resetFilters" class="btn-secondary mt-8">Xóa bộ lọc</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="px-24 py-16 border-t border-border/80 flex items-center justify-between bg-white">
          <p class="text-[13px] text-secondary">
            Hiển thị <span class="font-bold text-primary">{{ (currentPage - 1) * itemsPerPage + 1 }}</span> – 
            <span class="font-bold text-primary">{{ Math.min(currentPage * itemsPerPage, totalItems) }}</span> trong 
            <span class="font-bold text-primary">{{ totalItems }}</span> đơn hàng
          </p>
          <div class="flex items-center gap-8">
            <button @click="currentPage--" :disabled="currentPage === 1" class="btn-secondary h-32 w-32 p-0 flex items-center justify-center disabled:opacity-30">
              <ChevronLeft :size="14" />
            </button>
            <div class="flex items-center gap-4">
              <button 
                v-for="p in totalPages" :key="p"
                @click="currentPage = p"
                class="w-32 h-32 rounded-md text-[13px] font-bold transition-all"
                :class="currentPage === p ? 'bg-primary text-white shadow-md' : 'text-secondary hover:bg-surface border border-border/40'"
              >
                {{ p }}
              </button>
            </div>
            <button @click="currentPage++" :disabled="currentPage === totalPages" class="btn-secondary h-32 w-32 p-0 flex items-center justify-center disabled:opacity-30">
              <ChevronRight :size="14" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Detail Slide-over Panel -->
    <div v-if="isPanelOpen" class="fixed inset-0 z-[100] overflow-hidden">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-[2px]" @click="closePanel"></div>
      <div class="absolute inset-y-0 right-0 max-w-full flex">
        <div class="w-screen max-w-md animate-in slide-in-from-right duration-300">
          <div class="h-full flex flex-col bg-white shadow-2xl">
            <div class="px-24 py-20 border-b border-border flex items-center justify-between bg-surface/30">
               <div class="flex flex-col">
                  <h2 class="text-title text-[18px]">Chi tiết đơn hàng</h2>
                  <span class="text-mono text-[13px] text-secondary font-bold">#{{ selected?.order?.id }}</span>
               </div>
               <button @click="closePanel" class="btn-ghost p-4 rounded-full">
                  <X :size="20" />
               </button>
            </div>

            <div class="flex-1 overflow-y-auto p-24 space-y-32">
               <!-- Status Block -->
               <div class="bg-surface rounded-card p-16 border border-border/60">
                  <div class="flex items-center gap-12 mb-16">
                     <div class="w-40 h-40 rounded-full bg-primary flex items-center justify-center text-white">
                        <component :is="getStatusInfo(selected?.order?.status).icon" :size="20" />
                     </div>
                     <div>
                        <p class="text-[12px] font-bold text-secondary uppercase tracking-wider">Trạng thái hiện tại</p>
                        <p class="text-[16px] font-bold text-primary">{{ getStatusInfo(selected?.order?.status).label }}</p>
                     </div>
                  </div>
                  <div class="grid grid-cols-2 gap-12">
                     <button class="btn-primary h-36 text-xs uppercase tracking-widest font-bold">Cập nhật</button>
                     <button class="btn-secondary h-36 text-xs uppercase tracking-widest font-bold">Hủy đơn</button>
                  </div>
               </div>

               <!-- Customer Block -->
               <div class="space-y-16">
                  <div class="flex items-center gap-8">
                     <User :size="16" class="text-secondary" />
                     <h3 class="text-section text-[14px]">Khách hàng</h3>
                  </div>
                  <div class="space-y-8 pl-24">
                     <p class="font-bold text-primary">{{ selected?.order?.account?.fullname }}</p>
                     <p class="text-sm text-secondary flex items-center gap-8"><Mail :size="12" /> {{ selected?.order?.account?.email }}</p>
                     <p class="text-sm text-secondary flex items-center gap-8"><Phone :size="12" /> {{ selected?.order?.account?.phone || '—' }}</p>
                     <p class="text-sm text-secondary flex items-center gap-8"><MapPin :size="12" class="shrink-0" /> {{ selected?.order?.address }}</p>
                  </div>
               </div>

               <!-- Items Block -->
               <div class="space-y-16">
                  <div class="flex items-center gap-8">
                     <Package :size="16" class="text-secondary" />
                     <h3 class="text-section text-[14px]">Sản phẩm đã chọn</h3>
                  </div>
                  <div class="space-y-12">
                     <div v-for="item in selected?.items" :key="item.id" class="flex gap-12 group">
                        <div class="w-48 h-48 rounded bg-surface border border-border shrink-0 overflow-hidden">
                           <img :src="'/images/' + item.productImage" class="w-full h-full object-cover" alt="">
                        </div>
                        <div class="flex-1 min-w-0">
                           <p class="text-sm font-bold text-primary truncate">{{ item.productName }}</p>
                           <p class="text-xs text-secondary">{{ item.quantity }} x {{ formatMoney(item.unitPrice) }}</p>
                        </div>
                        <div class="text-right">
                           <p class="text-sm font-bold text-primary">{{ formatMoney(item.lineTotal) }}</p>
                        </div>
                     </div>
                  </div>
               </div>

               <!-- Payment Block -->
               <div class="pt-24 border-t border-border space-y-12">
                  <div class="flex justify-between text-secondary">
                     <span>Tạm tính</span>
                     <span>{{ formatMoney(selected?.order?.finalTotal) }}</span>
                  </div>
                  <div class="flex justify-between text-secondary">
                     <span>Phí vận chuyển</span>
                     <span class="text-success">Miễn phí</span>
                  </div>
                  <div class="flex justify-between items-end pt-8">
                     <span class="font-bold text-primary">Tổng thanh toán</span>
                     <span class="text-[20px] font-bold text-primary">{{ formatMoney(selected?.order?.finalTotal) }}</span>
                  </div>
               </div>
            </div>

            <div class="p-24 border-t border-border bg-surface/30">
               <button class="btn-secondary w-full gap-8">
                  <ExternalLink :size="16" />
                  Xem hóa đơn chi tiết
               </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
