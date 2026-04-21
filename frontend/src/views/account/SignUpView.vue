<script setup>
import {SignUpPage} from "@/legacy/pages";

const {form, message, submit} = SignUpPage.setup();
</script>

<template>
    <main class="container auth-page">
        <div class="auth-card">
            <div class="auth-card__header">
                <h1 class="auth-card__title">Tạo tài khoản</h1>
                <p class="auth-card__subtitle">Bắt đầu hành trình mua sắm của bạn.</p>
            </div>
            <div class="auth-card__body">
                <div v-if="message" class="alert alert--error mb-3">{{ message }}</div>
                <form class="auth-form" @submit.prevent="submit">
                    <div class="form-group">
                        <label for="username">Tài khoản</label>
                        <input id="username" type="text" v-model="form.username" class="form-control" placeholder="Tạo tài khoản" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Mật khẩu</label>
                        <input id="password" type="password" v-model="form.password" class="form-control" placeholder="Tạo mật khẩu" required>
                    </div>
                    <div class="form-group">
                        <label for="fullname">Họ tên</label>
                        <input id="fullname" type="text" v-model="form.fullname" class="form-control" placeholder="Nhập họ tên" required>
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input id="email" type="email" v-model="form.email" class="form-control" placeholder="Nhập email" required>
                    </div>
                    <button class="btn btn--primary btn--block" type="submit">Đăng ký</button>
                </form>
            </div>
            <div class="auth-card__footer">
                <p>Đã có tài khoản? <router-link to="/auth/login">Đăng nhập ngay</router-link></p>
            </div>
        </div>
    </main>
</template>
