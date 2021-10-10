/*
 Navicat Premium Data Transfer

 Source Server         : klz
 Source Server Type    : MySQL
 Source Server Version : 80024
 Source Host           : localhost:3306
 Source Schema         : iblog

 Target Server Type    : MySQL
 Target Server Version : 80024
 File Encoding         : 65001

 Date: 10/10/2021 23:10:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '博客id',
  `title` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '博客标题',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '博客描述',
  `img` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '博客图片',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '博客内容',
  `publish` tinyint(1) NULL DEFAULT 0 COMMENT '是否发布,0未保存、1收藏、2草稿',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '博客是否原创',
  `thumb` int NULL DEFAULT 1000 COMMENT '点赞量',
  `browse` int NULL DEFAULT 1000 COMMENT '浏览量',
  `collection` int NULL DEFAULT 1000 COMMENT '收藏量',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `classify_id` int NOT NULL COMMENT '分类id',
  `collection_id` int NULL DEFAULT NULL COMMENT '收藏id',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog
-- ----------------------------

-- ----------------------------
-- Table structure for classify
-- ----------------------------
DROP TABLE IF EXISTS `classify`;
CREATE TABLE `classify`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分类名称',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of classify
-- ----------------------------

-- ----------------------------
-- Table structure for collection
-- ----------------------------
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收藏id',
  `name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收藏名称',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collection
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '评论内容',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `blog_id` int NOT NULL COMMENT '博客id',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '留言id',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '留言内容',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '通知id',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知内容',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '资源id',
  `type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源类型',
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名称',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '访问的url',
  `pid` int NULL DEFAULT NULL COMMENT '父节点id',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '资源状态',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图标',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 'menu', ' 用户管理', '/userList', NULL, 1, '2021-10-05', NULL, NULL, NULL);
INSERT INTO `permission` VALUES (2, 'button', '编辑用户', ' user:edit', NULL, 1, '2021-10-05', NULL, NULL, NULL);
INSERT INTO `permission` VALUES (3, 'button', ' 添加用户', 'user:add', NULL, 1, '2021-10-05', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for reply
-- ----------------------------
DROP TABLE IF EXISTS `reply`;
CREATE TABLE `reply`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '回复id',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '回复内容',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `blog_id` int NOT NULL COMMENT '博客id',
  `user_id` int NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reply
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '角色状态',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'admin', 1, '2021-10-05', NULL, NULL);
INSERT INTO `role` VALUES (2, 'customer', 1, '2021-10-05', NULL, NULL);

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色权限id',
  `role_id` int NOT NULL COMMENT '角色id',
  `permission_id` int NOT NULL COMMENT '资源id',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 1, 1, '2021-10-05', NULL, NULL);
INSERT INTO `role_permission` VALUES (2, 1, 2, '2021-10-05', NULL, NULL);
INSERT INTO `role_permission` VALUES (3, 1, 3, '2021-10-05', NULL, NULL);
INSERT INTO `role_permission` VALUES (4, 2, 1, '2021-10-05', NULL, NULL);

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签id',
  `name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标签名称',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  `blog_id` int NOT NULL COMMENT '博客id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `email` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `bgmusic` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '背景音乐',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '用户的状态',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updaeTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '1822521315@qq.com', 'klz', ' 1822521315@qq.com', NULL, NULL, 1, '2021-10-05', NULL, NULL);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户角色id',
  `user_id` int NOT NULL COMMENT '用户id',
  `role_id` int NOT NULL COMMENT '角色id',
  `createTime` date NULL DEFAULT NULL COMMENT '创建时间',
  `updateTime` date NULL DEFAULT NULL COMMENT '修改时间',
  `deleteTime` date NULL DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1, '2021-10-05', NULL, NULL);
INSERT INTO `user_role` VALUES (2, 1, 2, '2021-10-05', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
