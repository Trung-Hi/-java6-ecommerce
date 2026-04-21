<script setup>
import {MyProductListPage} from "@/legacy/pages";
import {api} from "@/api";
import {useRouter} from "vue-router";

const {rows, error, load, money} = MyProductListPage.setup();
const router = useRouter();

const buyAgain = async (detail) => {
    const productId = detail.product?.id || detail.productId;
    const sizeId = detail.sizeId;
    const quantity = Number(detail.quantity || 1);
    if (!productId || !sizeId) {
        return;
    }
    try {
        await api.cart.addDetail(productId, sizeId, quantity > 0 ? quantity : 1);
    } catch (e) {
    }
    await router.push(`/cart/index`);
};
</script>

<template>
    <main class="container page-shell">
        <h3 class="page-title">Sản phẩm đã mua</h3>
        <div v-if="error" class="status-message status-error">{{ error }}</div>
        <div class="card">
            <table>
                <thead>
                <tr><th>Sản phẩm</th><th>Giá</th><th>Số lượng</th><th>Size</th><th></th></tr>
                </thead>
                <tbody>
                <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.product?.name }}</td>
                    <td>{{ money(r.price) }} <small>đ</small></td>
                    <td>{{ r.quantity }}</td>
                    <td>{{ r.sizeName }}</td>
                    <td><button class="btn btn-outline" type="button" @click="buyAgain(r)">Mua lại</button></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="table-actions" style="margin-top:10px;">
            <button class="btn" type="button" @click="load">Tải dữ liệu</button>
        </div>
    </main>
</template>
