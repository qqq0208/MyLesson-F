import api from "../../../utils/api.js";
import util from "../../../utils/util.js";
import constant from "../../../utils/const.js";

Page({
    data: {
        phone: '17766541438', // 手机号码
        vcode: '', // 短信验证码
    },

    // 获取短信验证码
    getVcode: function () {
        let that = this;
        let phone = this.data.phone;

        // 检验手机号码
        if (!constant.RULE.PHONE[0]['pattern'].test(phone)) {
            util.tip(constant.RULE.PHONE[0]['message']);
            return;
        }

        // 发送请求：根据手机号码获取短信验证码
        api.get('user', '/getVcode/' + phone).then(res => {
                util.success('验证码获取成功');
                that.setData({vcode: res});
            }
        ).catch(err => console.error(err));
    },

    // 登录
    loginByPhone: function () {
        let phone = this.data.phone;
        let vcode = this.data.vcode;

        // 检验手机号码
        if (!constant.RULE.PHONE[0]['pattern'].test(phone)) {
            util.tip(constant.RULE.PHONE[0]['message']);
            return;
        }

        // 校验验证码
        if (util.isEmpty(vcode)) {
            util.tip('验证码为空');
            return;
        }

        // 发送登录请求
        api.post('user', '/loginByPhone', {phone, vcode}).then(res => {
            // 将用户信息以及对应的Token令牌存储起来
            wx.setStorageSync('token', res['token']);
            wx.setStorageSync('user', res['user']);
            util.success('登录成功');
            // 0.5秒后切换到 "首页" 选项卡
            setTimeout(() => util.tab('/pages/index/index'), 500);
        }).catch(err => console.error(err));
    },

    // 跳转到注册页面
    toRegister: function () {
        util.page('/pages/index/register/register', false);
    },

    // 跳转到账号登录页面
    toLoginByAccount: function () {
        util.page('/pages/index/login-by-account/login-by-account', false);
    },

    // 返回首页
    toIndex: function () {
        util.tab('/pages/index/index');
    },

    // 加载函数
    onLoad: function (options) {
        wx.removeStorageSync('token');
        wx.removeStorageSync('user');
    }
});