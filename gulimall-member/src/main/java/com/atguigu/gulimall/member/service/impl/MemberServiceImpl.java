package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UserNameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.SocialUserDetail;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo memberRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();
        // 设置默认等级
//        MemberLevelEntity levelEntity =memberLevelDao.getDefaultLevel();
        MemberLevelEntity levelEntity = memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        memberEntity.setLevelId(levelEntity.getId());


        // 检查用户名和手机号是否唯一 为了让controller 能感知 ，异常机制
        checkPhone(memberRegisterVo.getPhone());
        checkUserName(memberEntity.getUsername());

        memberEntity.setMobile(memberRegisterVo.getPhone());
        memberEntity.setUsername(memberRegisterVo.getUserName());

        // 密码进行加密存储

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        //其他的默认信息

        // 保存
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhone(String phone) throws PhoneExistException {
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserName(String userName) throws UserNameExistException {

        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0)
            throw new UserNameExistException();
    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {

        String loginacct = memberLoginVo.getLoginacct();
        String password = memberLoginVo.getPassword();

        // 去数据库查询
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct)
                .or()
                .eq("mobile", loginacct));

        if (memberEntity != null) {
            // 获取到数据库password
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordDb);
            if (matches) {
                return memberEntity;
            }
        }

        // 登录失败 （没查到用户或者查到了密码匹配不上）
        return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {

        // 登录和注册合并逻辑
        String id = socialUser.getId();
        //1、判断当前社交用户是否已经登陆过系统
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_gitee_uid", id));
        if (memberEntity != null) {
            // 用户已经注册过了
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setExpiresGiteeIn(socialUser.getExpiresIn().toString());
            update.setAcessGiteeToken(socialUser.getAccessToken());

            baseMapper.updateById(update);

            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就注册一个(保存用户id和社交账户id之间关系)
            MemberEntity register = new MemberEntity();
            //3、查询当前社交用户账号信息（昵称，性别等）
            try {
                Map<String, String> queryMap = new HashMap<>();
                HashMap<String, String> header = new HashMap<>();
                queryMap.put("access_token", socialUser.getAccessToken());
                HttpResponse response = HttpUtils.doGet("https://gitee.com/", "api/v5/user", "get", header, queryMap);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String s = EntityUtils.toString(response.getEntity());
                    SocialUserDetail socialUserDetail = JSON.parseObject(s, SocialUserDetail.class);
//                JSONObject jsonObject = JSON.parseObject(s);
//                jsonObject.getString("name");
                    register.setNickname(socialUserDetail.getName());
                }
            } catch (Exception e) {

            }

            // 就算受网络影响远程查询失败，也要存这部分主要信息
            register.setSocialGiteeUid(socialUser.getId());
            register.setExpiresGiteeIn(socialUser.getExpiresIn().toString());
            register.setAcessGiteeToken(socialUser.getAccessToken());
            baseMapper.insert(register);

            return register;

        }

    }

}





















