import api from '../../../utils/api.js';
import util from '../../../utils/util.js';
import constant from "../../../utils/const.js";

Page({
    data: {
        MINIO_COURSE_SUMMARY: constant.MINIO_COURSE_SUMMARY, // 视频封面MINIO地址
        course: null, // 课程对象
        videoSrc: null, // 免费视频地址
        videoPoster: null, // 免费视频封面
        videoTitle: null, // 免费视频标题
        welcomeBarrage: [{text: '一大波弹幕即将来袭', color: '#ff0000', time: 1}], // 欢迎弹幕
        activeTab: '摘要' // 当前选中的tab的name值
    },

    // 查询视频详情
    getCourseInfo: function (courseId) {
        let that = this;
        let param = '/select/' + courseId;
        api.get('course', param).then(res => {
            res['created'] = util.dateFormat(res['created']);
            res['updated'] = util.dateFormat(res['updated']);
            if (res['seasons'].length > 0 && res['seasons'][0]['episodes'].length > 0) {
                let firstEpisode = res['seasons'][0]['episodes'][0];
                that.setData({
                    'videoSrc': constant.MINIO_EPISODE_VIDEO + firstEpisode['video'],
                    'videoPoster': constant.MINIO_EPISODE_VIDEO_COVER + firstEpisode['cover'],
                    'videoTitle': firstEpisode['title'],
                });
            }
            that.setData({'course': res});
        }).catch(err => console.error(err));
    },

    // 跳转到购物车页面
    toCart: function () {
        if (util.isLogin()) {
            util.tab('/pages/cart/cart');
        }
    },

    // 添加购物车
    addToCart: function () {
        if (util.isLogin()) {
            let that = this;
            let params = {
                'fkUserId': wx.getStorageSync('user').id,
                "fkCourseId": that.data.course['id'],
            };
            api.post('cart', '/insert', params).then(res => {
                util.error('加购成功');
                setTimeout(() => {
                    util.tab('/pages/cart/cart', true);
                }, 500);
            }).catch(err => console.error(err))
        }
    },

    // 客服
    chatMe: function () {
        util.page('/pages/course/detail/chat/chat')
    },

    // 立即购买
    pay: function () {
        if (util.isLogin()) {
            util.error('功能暂未开放');
        }
    },

    // 加载函数
    onLoad: function (ev) {
        // 查询视频详情：获取路径传递过来的值
        this.getCourseInfo(ev['courseId']);
    }
});