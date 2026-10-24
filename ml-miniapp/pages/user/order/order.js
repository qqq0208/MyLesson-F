import util from "../../../utils/util.js";
import api from "../../../utils/api.js";
import constant from "../../../utils/const.js";

Page({
    data: {
        MINIO_COURSE_COVER: constant.MINIO_COURSE_COVER, // 课程封面MINIO地址
        pageInfo: {pageNum: 1, pageSize: 10, totalPage: 0, totalRow: 0}, // 分页信息
        orders: null, // 订单记录
    },

    // 分页查询订单记录
    page: function () {
        let that = this;
        let pageNum = that.data.pageInfo['pageNum'];
        let pageSize = that.data.pageInfo['pageSize'];
        api.get('order', '/page', {
            'pageNum': pageNum,
            'pageSize': pageSize,
            'fkUserId': wx.getStorageSync('user').id
        }).then(res => {
            that.setData({
                'orders': pageNum === 1 ? res['records'] : that.data.orders.concat(res['records']),
                'pageInfo.pageNum': res['pageNumber'],
                'pageInfo.pageSize': res['pageSize'],
                'pageInfo.totalPage': res['totalPage'],
                'pageInfo.totalRow': res['totalRow'],
            });
        }).catch(err => console.log(err));
    },

    // 列表触底时追查下一页记录
    pageMore: function () {
        let that = this;
        let pageNum = that.data.pageInfo['pageNum'];
        let totalPage = that.data.pageInfo['totalPage'];
        if (pageNum < totalPage) {
            this.setData({'pageInfo.pageNum': this.data.pageInfo['pageNum'] + 1});
            this.page();
        }
    },

    // 删除订单记录
    removeOrder: function (ev) {
        let id = ev.currentTarget.dataset['id'];
        util.confirm('整单课程将全部被删除，确定吗？', () => {
            api.del('order', `/delete/${id}`).then(res => {
                util.success('订单删除成功');
                this.setData({'orders': null});
                this.page();
            }).catch(err => console.error(err));
        });
    },

    // 播放视频
    playVideo: function (ev) {
        let courseId = ev.currentTarget.dataset['courseId'];
        util.page('/pages/user/order/player/player?courseId=' + courseId, false);
    },

    // 加载函数
    onLoad: function (options) {
        if (util.isLogin()) {
            this.page();
        }
    }
});