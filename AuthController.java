package jp.co.internous.bloom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.bloom.model.domain.MstUser;
import jp.co.internous.bloom.model.form.UserForm;
import jp.co.internous.bloom.model.mapper.MstUserMapper;
import jp.co.internous.bloom.model.mapper.TblCartMapper;
import jp.co.internous.bloom.model.session.LoginSession;

@RestController
@RequestMapping("/bloom/auth")
public class AuthController{
	
	private Gson gson=new Gson();
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private TblCartMapper cartMapper;
	
	@Autowired
	private LoginSession loginSession;
	
	@RequestMapping("/login")
	public String login(@RequestBody UserForm form){
		MstUser user=userMapper.findByUserNameAndPassword(form.getUserName(),form.getPassword());
		
		int tmpUserId=loginSession.getTmpUserId();
		if(user != null && tmpUserId != 0){
			
			int count=cartMapper.findCountByUserId(tmpUserId);
			if(count > 0){
				cartMapper.updateUserId(user.getId(),tmpUserId);
			}
		}
		
		if(user != null){
			loginSession.setTmpUserId(0);
			loginSession.setLogined(true);
			loginSession.setUserId(user.getId());
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
		}else{
			loginSession.setLogined(false);
			loginSession.setUserId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);
		}
		
		return gson.toJson(user);
	}
	
	@RequestMapping("/logout")
	public String logout(){
		loginSession.setTmpUserId(0);
		loginSession.setLogined(false);
		loginSession.setUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		
		return "";
	}
	
	@RequestMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm form){
		String newPassword=form.getNewPassword();
		String newPasswordConfirm=form.getNewPasswordConfirm();
		
		MstUser user=userMapper.findByUserNameAndPassword(form.getUserName(),form.getPassword());
		if(user == null){
			return "現在のパスワードが正しくありません。";
		}
		
		if(user.getPassword().equals(newPassword)){
			return "現在のパスワードと同一文字列が入力されました。";
		}
		
		if(!newPassword.equals(newPasswordConfirm)){
			return "新パスワードと確認用パスワードが一致しません。";
		}
		
		userMapper.updatePassword(user.getUserName(),form.getNewPassword());
		loginSession.setPassword(form.getNewPassword());
		
		return "パスワードが再設定されました。";
	}
}