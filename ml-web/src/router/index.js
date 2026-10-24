import {createRouter, createWebHashHistory} from "vue-router";
import {ElMessage} from "element-plus";
import vuex from "../vuex/index.js";
import Login from "../views/Login.vue";
import Main from "../views/Main.vue";
import Dashboard from "../views/Dashboard.vue";
import Personal from "../views/personal/Personal.vue";
import PersonalUpdate from "../views/personal/PersonalUpdate.vue";
import PersonalUpdatePhone from '../views/personal/PersonalUpdatePhone.vue';
import User from "../views/ums/user/User.vue";
import UserInsert from "../views/ums/user/UserInsert.vue";
import UserUpdate from "../views/ums/user/UserUpdate.vue";
import UserUpdateRoles from "../views/ums/user/UserUpdateRoles.vue";
import Role from "../views/ums/role/Role.vue";
import RoleInsert from "../views/ums/role/RoleInsert.vue";
import RoleUpdate from "../views/ums/role/RoleUpdate.vue";
import RoleUpdateMenus from "../views/ums/role/RoleUpdateMenus.vue";
import Menu from "../views/ums/menu/Menu.vue";
import MenuInsert from "../views/ums/menu/MenuInsert.vue";
import MenuUpdate from "../views/ums/menu/MenuUpdate.vue";
import SubMenu from "../views/ums/menu/sub/SubMenu.vue";
import SubMenuInsert from "../views/ums/menu/sub/SubMenuInsert.vue";
import SubMenuUpdate from "../views/ums/menu/sub/SubMenuUpdate.vue";
import Category from "../views/cms/category/Category.vue";
import CategoryInsert from "../views/cms/category/CategoryInsert.vue";
import CategoryUpdate from "../views/cms/category/CategoryUpdate.vue";
import Course from "../views/cms/course/Course.vue";
import CourseInsert from "../views/cms/course/CourseInsert.vue";
import CourseUpdate from "../views/cms/course/CourseUpdate.vue";
import Season from "../views/cms/course/season/Season.vue";
import SeasonInsert from "../views/cms/course/season/SeasonInsert.vue";
import SeasonUpdate from "../views/cms/course/season/SeasonUpdate.vue";
import Episode from "../views/cms/course/episode/Episode.vue";
import EpisodeInsert from "../views/cms/course/episode/EpisodeInsert.vue";
import EpisodeUpdate from "../views/cms/course/episode/EpisodeUpdate.vue";
import Comment from "../views/cms/comment/Comment.vue";
import CommentInsert from "../views/cms/comment/CommentInsert.vue";
import CommentUpdate from "../views/cms/comment/CommentUpdate.vue";
import SubComment from "../views/cms/comment/sub/SubComment.vue";
import SubCommentInsert from "../views/cms/comment/sub/SubCommentInsert.vue";
import SubCommentUpdate from "../views/cms/comment/sub/SubCommentUpdate.vue";
import Report from "../views/cms/report/Report.vue";
import ReportInsert from "../views/cms/report/ReportInsert.vue";
import ReportUpdate from "../views/cms/report/ReportUpdate.vue";
import Notice from "../views/sms/notice/Notice.vue";
import NoticeInsert from "../views/sms/notice/NoticeInsert.vue";
import NoticeUpdate from "../views/sms/notice/NoticeUpdate.vue";
import Article from "../views/sms/article/Article.vue";
import ArticleInsert from "../views/sms/article/ArticleInsert.vue";
import ArticleUpdate from "../views/sms/article/ArticleUpdate.vue";
import Banner from "../views/sms/banner/Banner.vue";
import BannerInsert from "../views/sms/banner/BannerInsert.vue";
import BannerUpdate from "../views/sms/banner/BannerUpdate.vue";
import Seckill from "../views/sms/seckill/Seckill.vue";
import SeckillInsert from "../views/sms/seckill/SeckillInsert.vue";
import SeckillUpdate from "../views/sms/seckill/SeckillUpdate.vue";
import SeckillDetail from "../views/sms/seckill/detail/SeckillDetail.vue";
import SeckillDetailInsert from "../views/sms/seckill/detail/SeckillDetailInsert.vue";
import SeckillDetailUpdate from "../views/sms/seckill/detail/SeckillDetailUpdate.vue";
import Coupons from "../views/sms/coupons/Coupons.vue";
import CouponsInsert from "../views/sms/coupons/CouponsInsert.vue";
import CouponsUpdate from "../views/sms/coupons/CouponsUpdate.vue";
import Cart from "../views/oms/cart/Cart.vue";
import CartInsert from "../views/oms/cart/CartInsert.vue";
import CartUpdate from "../views/oms/cart/CartUpdate.vue";
import Order from "../views/oms/order/Order.vue";
import OrderInsert from "../views/oms/order/OrderInsert.vue";
import OrderUpdate from "../views/oms/order/OrderUpdate.vue";
import OrderDetail from "../views/oms/order/detail/OrderDetail.vue";
import OrderDetailInsert from "../views/oms/order/detail/OrderDetailInsert.vue";
import OrderDetailUpdate from "../views/oms/order/detail/OrderDetailUpdate.vue";

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {path: '/', name: 'Login', component: Login},
        {
            path: '/Main', name: 'Main', component: Main,
            redirect: '/Dashboard',
            children: [
                {path: '/Dashboard', name: 'Dashboard', component: Dashboard},
                {path: '/Personal', name: 'Personal', component: Personal},
                {path: '/PersonalUpdate', name: 'PersonalUpdate', component: PersonalUpdate},
                {path: '/PersonalUpdatePhone', name: 'PersonalUpdatePhone', component: PersonalUpdatePhone},
                {path: '/User', name: 'User', component: User},
                {path: '/UserInsert', name: 'UserInsert', component: UserInsert},
                {path: '/UserUpdate', name: 'UserUpdate', component: UserUpdate},
                {path: '/UserUpdateRoles', name: 'UserUpdateRoles', component: UserUpdateRoles},
                {path: '/Role', name: 'Role', component: Role},
                {path: '/RoleInsert', name: 'RoleInsert', component: RoleInsert},
                {path: '/RoleUpdate', name: 'RoleUpdate', component: RoleUpdate},
                {path: '/RoleUpdateMenus', name: 'RoleUpdateMenus', component: RoleUpdateMenus},
                {path: '/Menu', name: 'Menu', component: Menu},
                {path: '/MenuInsert', name: 'MenuInsert', component: MenuInsert},
                {path: '/MenuUpdate', name: 'MenuUpdate', component: MenuUpdate},
                {path: '/SubMenu', name: 'SubMenu', component: SubMenu},
                {path: '/SubMenuInsert', name: 'SubMenuInsert', component: SubMenuInsert},
                {path: '/SubMenuUpdate', name: 'SubMenuUpdate', component: SubMenuUpdate},
                {path: '/Category', name: 'Category', component: Category},
                {path: '/CategoryInsert', name: 'CategoryInsert', component: CategoryInsert},
                {path: '/CategoryUpdate', name: 'CategoryUpdate', component: CategoryUpdate},
                {path: '/Course', name: 'Course', component: Course},
                {path: '/CourseInsert', name: 'CourseInsert', component: CourseInsert},
                {path: '/CourseUpdate', name: 'CourseUpdate', component: CourseUpdate},
                {path: '/Season', name: 'Season', component: Season},
                {path: '/SeasonInsert', name: 'SeasonInsert', component: SeasonInsert},
                {path: '/SeasonUpdate', name: 'SeasonUpdate', component: SeasonUpdate},
                {path: '/Episode', name: 'Episode', component: Episode},
                {path: '/EpisodeInsert', name: 'EpisodeInsert', component: EpisodeInsert},
                {path: '/EpisodeUpdate', name: 'EpisodeUpdate', component: EpisodeUpdate},
                {path: '/Comment', name: 'Comment', component: Comment},
                {path: '/CommentInsert', name: 'CommentInsert', component: CommentInsert},
                {path: '/CommentUpdate', name: 'CommentUpdate', component: CommentUpdate},
                {path: '/SubComment', name: 'SubComment', component: SubComment},
                {path: '/SubCommentInsert', name: 'SubCommentInsert', component: SubCommentInsert},
                {path: '/SubCommentUpdate', name: 'SubCommentUpdate', component: SubCommentUpdate},
                {path: '/Report', name: 'Report', component: Report},
                {path: '/ReportInsert', name: 'ReportInsert', component: ReportInsert},
                {path: '/ReportUpdate', name: 'ReportUpdate', component: ReportUpdate},
                {path: '/Notice', name: 'Notice', component: Notice},
                {path: '/NoticeInsert', name: 'NoticeInsert', component: NoticeInsert},
                {path: '/NoticeUpdate', name: 'NoticeUpdate', component: NoticeUpdate},
                {path: '/Article', name: 'Article', component: Article},
                {path: '/ArticleInsert', name: 'ArticleInsert', component: ArticleInsert},
                {path: '/ArticleUpdate', name: 'ArticleUpdate', component: ArticleUpdate},
                {path: '/Banner', name: 'Banner', component: Banner},
                {path: '/BannerInsert', name: 'BannerInsert', component: BannerInsert},
                {path: '/BannerUpdate', name: 'BannerUpdate', component: BannerUpdate},
                {path: '/Seckill', name: 'Seckill', component: Seckill},
                {path: '/SeckillInsert', name: 'SeckillInsert', component: SeckillInsert},
                {path: '/SeckillUpdate', name: 'SeckillUpdate', component: SeckillUpdate},
                {path: '/SeckillDetail', name: 'SeckillDetail', component: SeckillDetail},
                {path: '/SeckillDetailInsert', name: 'SeckillDetailInsert', component: SeckillDetailInsert},
                {path: '/SeckillDetailUpdate', name: 'SeckillDetailUpdate', component: SeckillDetailUpdate},
                {path: '/Coupons', name: 'Coupons', component: Coupons},
                {path: '/CouponsInsert', name: 'CouponsInsert', component: CouponsInsert},
                {path: '/CouponsUpdate', name: 'CouponsUpdate', component: CouponsUpdate},
                {path: '/Cart', name: 'Cart', component: Cart},
                {path: '/CartInsert', name: 'CartInsert', component: CartInsert},
                {path: '/CartUpdate', name: 'CartUpdate', component: CartUpdate},
                {path: '/Order', name: 'Order', component: Order},
                {path: '/OrderInsert', name: 'OrderInsert', component: OrderInsert},
                {path: '/OrderUpdate', name: 'OrderUpdate', component: OrderUpdate},
                {path: '/OrderDetail', name: 'OrderDetail', component: OrderDetail},
                {path: '/OrderDetailInsert', name: 'OrderDetailInsert', component: OrderDetailInsert},
                {path: '/OrderDetailUpdate', name: 'OrderDetailUpdate', component: OrderDetailUpdate},
            ]
        }
    ]
});

/**
 * 路由前置守卫：每次转发路由前执行如下判断：
 * 1. 若页面为登录页面，或已登录状态，均直接放行。
 * 2. 其余情况均给出 “请先登录” 提示，并在两秒后跳转回登录页面。
 *
 * @param to: 来源地址
 * @param from: 目标地址
 */
router.beforeEach((to, from, next) => {
    if (to.path === '/' || vuex.state['loginFlag']) {
        next();
    }
    else {
        ElMessage.warning('请先登录！');
        setTimeout(() => next('/'), 2000);
    }
});

export default router
