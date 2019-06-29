package yemu.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import yemu.domain.User;
import yemu.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @RequestMapping("/ShowUser")
    public String toIndex(HttpServletRequest request, Model model){
        int userId=Integer.parseInt(request.getParameter("id"));
        User user=this.userService.getUserById(userId);
        model.addAttribute("user",user);
        return "ShowUser";
    }
    @ResponseBody
    @RequestMapping(value = "/addUser",produces = "application/json;charset=UTF-8")
    public Object addUser(HttpServletRequest request){
        String account=request.getParameter("account");
        String password=request.getParameter("password");
        String name=request.getParameter("name");
        if (userService.getUserByAccount(account)!=null){
            return JSONObject.toJSONString("添加失败！账号重复");
        }
        if (userService.getUserByName(name)!=null){
            return JSONObject.toJSONString("添加失败！名字重复");
        }
        if (password.trim().equals("")){
            return JSONObject.toJSONString("添加失败！密码为空");
        }
        User user=new User();
        user.setAccount(account);
        user.setName(name);
        user.setPassword(password);
        userService.addUser(user);
        return JSONObject.toJSONString("ok");
    }
    @ResponseBody
    @RequestMapping(value = "/getUser",produces = "application/json;charset=UTF-8",params = "id")
    public Object getUserById(HttpServletRequest request){//根据id获取用户信息
        try{
            String id=request.getParameter("id");
            id=id.trim();//去除多余空格
            if (id==null||id=="")
                return null;
            Integer userId=Integer.parseInt(id);
            User user=this.userService.getUserById(userId);
            return JSONObject.toJSONString(user);
        }
        catch (Exception e){
            e.printStackTrace();
        }
//        System.out.println(JSONObject.toJSONString(user));
        return null;
    }
    @ResponseBody
    @RequestMapping(value = "/getUser",produces = "application/json;charset=UTF-8",params = "account")
    public Object getUserByAccount(HttpServletRequest request){//根据账号获取信息
        try{
            String account=request.getParameter("account");
            account=account.trim();
            if (account==null||account.equals("")){
                return null;
            }
            User user=this.userService.getUserByAccount(account);
            return JSONObject.toJSONString(user);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @ResponseBody
    @RequestMapping(value = "/getUser",produces = "application/json;charset=UTF-8",params = "name")
    public Object getUserByName(HttpServletRequest request){//根据姓名获取
        try{
            String name=request.getParameter("name");
            name=name.trim();
            if (name==null||name.equals("")){
                return null;
            }
            User user=this.userService.getUserByName(name);
            return JSONObject.toJSONString(user);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/login",produces = "application/json;charset=UTF-8")
    public Object login(HttpServletRequest request, HttpServletResponse response){//登录
        JSONObject json=new JSONObject(true);
        try{
            String account=request.getParameter("account");
            String password=request.getParameter("password");
            String remember=request.getParameter("remember");
            if (remember.equals("false")||remember==null){//不记住登录状态
                remember="";
                Cookie uid=new Cookie("uid",null);//删除cookie
                uid.setMaxAge(0);//生存时间为0
                uid.setPath("/");//相同路径
                response.addCookie(uid);
            }
            User user=userService.getUserByAccount(account);
            if (user!=null) {
                if (user.getPassword().equals(password)) {
                    json.put("return", "success");
                    json.put("id", user.getId());
                    json.put("account", user.getName());
                    json.put("name", user.getName());
                    json.put("create_time", user.getCreate_time());
                    if (remember.equals("true")){//记住登录状态
                        Cookie uid=new Cookie("uid",user.getId().toString());
                        uid.setPath("/");
                        uid.setMaxAge(7*24*60*60);//一周过期
                        response.addCookie(uid);
                    }
                    HttpSession session=request.getSession();
                    session.setAttribute("user",user);


                } else {
                    json.put("return", "failed");
                }
            }
            else{
                json.put("return","none");
            }
        }catch (Exception e){
            e.printStackTrace();
            json.put("return","error");
        }
        return json.toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/getCount",produces = "application/json;charset=UTF-8")
    public Object getCount(){//获取总数
        Integer count=0;
        try {
            count=userService.getCount();
        }
        catch (Exception e){
            e.printStackTrace();
            count=0;
        }
        return JSONObject.toJSONString(count);
    }

    @ResponseBody
    @RequestMapping(value = "/getAll",produces = "application/json;charset=UTF-8")
    public Object getAll(){//获取所有用户信息
        List<User> users = null;
        try {
            users=userService.getAll();
        }catch (Exception e){
            e.printStackTrace();
        }
        return JSONObject.toJSONString(users);
    }

    @ResponseBody
    @RequestMapping(value = "/deleteById",produces = "application/json;charset=UTF-8",params = "id")
    public Object deleteById(HttpServletRequest request){
        String flag="error";
        try {
            if (request.getParameter("id")!=null&&request.getParameter("id").trim()!="") {
                Integer id= Integer.valueOf(request.getParameter("id"));
                userService.delete(id);//删除用户
                flag="success";
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return JSONObject.toJSONString(flag);
    }

    @ResponseBody
    @RequestMapping(value = "/update",produces = "application/json;charset=UTF-8",params = {"id","account","name"})
    public Object update(HttpServletRequest request){
        User user = null;
        try{
            Integer id= Integer.valueOf(request.getParameter("id"));
            String acount=request.getParameter("account");
            String name=request.getParameter("name");
            user=userService.getUserById(id);
            user.setName(name);
            user.setAccount(acount);
            userService.update(user);
            user=userService.getUserById(id);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return JSONObject.toJSONString(user);
    }
}