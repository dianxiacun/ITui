//页面加载函数
$(function() {
	$body = (window.opera) ? (document.compatMode == "CSS1Compat" ? $('html')
			: $('body')) : $('html,body');
	var wind_h = $(window).height();
	var wind_w = $(window).width();
	var mod_h = $('.tab').height();
	var mod_w = $('.tab').width();
	// 自定义模态框可见函数
	function load_modal(wind_w,wind_h,mod_w,mod_h)
	{
		// 定义模态框的绝对位置
		$('.tab').css
		({
			position: 'absolute',
			left: (wind_w-mod_w)/2,
			top: (wind_h-mod_h)/2
		});
	}
	
	function resize_modal()
	{	
		$(window).resize(function(event) 
		{	
			// 当窗口发生变化时得到动态的窗口宽高
			 wind_h=$(window).height();
			 wind_w=$(window).width();
			
			 mod_h=$('.tab').height();
			 mod_w=$('.tab').width();
			modal_position(wind_h,wind_w,mod_h,mod_w);

		});
	}
	
	function modal_position(wind_h, wind_w, mod_h, mod_w) {
		$('body').css('overflow-x', 'hidden');
		load_modal(wind_w, wind_h, mod_w, mod_h);
		var docu_height = $(document).height();
		$('#modal_load').css('height', docu_height);
		$('#gai_load').css('height', docu_height);

	}
	// 若用户没登陆直接显现模态框
	function modal_visiblity() {
		modal_position(wind_h, wind_w, mod_h, mod_w);
		resize_modal();
		$('#modal_load').css('display', 'block');
	}
	modal_visiblity();
	$('.ew_ma').hide();

	var user_value = $.cookie("user");
	if (user_value == undefined) {
		$('.user2').css('display', 'block');
		$('.user2').find('img').attr('src', 'images/user2.png');
		$('.user').css('display', 'none');
		// 判断当前页面是否是信息页
		var str_info = window.location.pathname;
		var str_texting = new RegExp('info');
		if (str_texting.test(str_info)) {
		} else {
			// lodal_close();
			$('body').css('overflow-y', 'visible');
			$('body').css('overflow-x', 'visible');
			$('#modal_load').css('display', 'none');
		}
		$('#modal_load').hide();
	} else {
		$('.user').css('display', 'block');
		$('.user2').css('display', 'none');
	}

	$('.d_down').click(function(event) {
		$('.dropdown-menu').css('display', 'block');
	});

	$('.zhuanye  a ').click(function(event) {
		$('.dropdown-menu').css('display', 'none');
		var neirong = $(this).html();
		var neirong2 = $('.nr_js').html();
		$('.nr_js').html(neirong);
		$('.zhuanye  a ').html(neirong2);
	});

	$('.back_top').click(function(event) {
		$body.animate({
			scrollTop : $('#nav0').offset().top
		}, 500);
		return false;
	});

	$('.user2').click(function(event) {
		// 当窗口发送变化时模态框的位置信息
		resize_modal();
		// 然后让模态框出现
		modal_visiblity()
	});

	$('#sou').click(function(event) {
		var major = $('.nr_js').text();
		var value = $('#scbar_txt').val();
		search_jump(major, value);
	});

	$('#scbar_txt').focus(function() {
		document.onkeydown = function(e) {
			var ev = document.all ? window.event : e;
			if (ev.keyCode == 13) {
				var major = $('.nr_js').text();
				var value = $('#scbar_txt').val();
				search_jump(major, value);
				console.log('focus');
			}
		}
	});

	$('#scbar_txt').blur(function() {
		document.onkeydown = function(e) {
			var ev = document.all ? window.event : e;
			if (ev.keyCode == 13) {
				console.log('blur');
			}
		}
	});

	$('.user').mouseenter(function(event) {
		$('.drop_menu').css('display', 'block');
		// 点击退出登录
		$('.out_coll').click(function(event) {
			$('.user').css('display', 'none');
			$('.user2').css('display', 'block');
			$('.user2').find('img').attr('src', 'images/user2.png');
			$('.drop_menu').remove();
			console.log($.cookie('user'));
			$.removeCookie('user', {
				path : '/'
			});
			window.location.reload();
		});
	}).mouseleave(function(event) {
		$('.drop_menu').css('display', 'none');
	});

	$('.collect').mouseenter(function(event) {
		$(this).find('.txt').css('color', '#5DB9E3');
	}).mouseleave(function(event) {
		$(this).find('.txt').css('color', '#666');
	});
	// 点击收藏列表跳转
	$('.jump_coll').click(function(event) {
		window.open("collect.html", "_blank");
	});

	$('.regs_mail')
			.blur(
					function(event) {
						var email02 = $('.regs_mail').val();
						var reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/;
						if (reg.test(email02)) {
							$('.errregs').html('√邮箱合法').css('color', '#6fd415');
							// 将正确的邮箱存进注册对象
							regs_obj.objemail = email02;
							// 判断是否两个输入框都合法
							submit_judge(regs_obj.objemail, regs_obj.objpasd);
						} else {

							$('.errregs').html('×邮箱不合法').css('color', 'red');
							regs_obj.objemail = 'null';
						}
						if (email02 == '') {
							$('.errregs').html('×邮箱不能为空').css('color', 'red');
							regs_obj.objemail = 'null';
						}
					});

	$('.regs_pasd').blur(function(event) {
		var pasd2 = $('.regs_pasd').val();
		var reg = /^[A-Za-z]\w{5,15}$/;
		if (reg.test(pasd2)) {
			$('.pasd_p').html('√密码合法').css('color', '#6fd415');
			// 注册确认密码
			$('.same_pasd').blur(function(event) {
				var pasd3 = $('.same_pasd').val();
				if (pasd2 == pasd3) {
					$('.same').html('√密码一致').css('color', '#6fd415');
					// 将正确的密码存进注册对象
					regs_obj.objpasd = pasd3;
					// 判断是否两个输入框都合法
					submit_judge(regs_obj.objemail, regs_obj.objpasd);

				} else {
					$('.same').html('×两次密码输入必须一致').css('color', 'red');
					regs_obj.objpasd = 'null';

				}
			});

		} else {
			$('.pasd_p').html('×输入6-15位以字母开头含有字母数字的密码').css('color', 'red');
			regs_obj.objpasd = 'null';

		}
		if (pasd2 == '') {
			$('.pasd_p').html('×密码不能为空').css('color', 'red');
			regs_obj.objpasd = 'null';
		}
	});

	$('.tab').css({
		position : 'absolute',
		left : (wind_w - mod_w) / 2,
		top : (wind_h - mod_h) / 2
	});

	// 注册按钮点击事件
	$('.regsitem').click(function(event) {
		$('.regspage').css('display', 'block');
		$('.loadpage').css('display', 'none');
		$('.regsitem').css('border-bottom', '2px solid #FF7F27');
		$('.loaditem').css('border', 'none');
	});
	// 登录按钮点击事件
	$('.loaditem').click(function(event) {
		$('.regspage').css('display', 'none');
		$('.loadpage').css('display', 'block');
		$('.regsitem').css('border', 'none');
		$('.loaditem').css('border-bottom', '2px solid #428BCA');
	});
	// 登录邮箱正则验证
	$('.mail_inp')
			.blur(
					function(event) {
						var email01 = $('.mail_inp').val();

						var reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/;

						if (reg.test(email01)) {
							$('.errmsg').html('√邮箱合法').css('color', '#6fd415');
							// 将登录邮箱存进登录对象中
							load_obj.objemail = email01;
							load_judge(load_obj.objemail, load_obj.objpasd);
						} else {
							$('.errmsg').html('×邮箱不合法');
						}
						if (email01 == '') {
							$('.errmsg').html('×邮箱不能为空');
						}
					});
	// 获得登录密码
	$('.pasd_inp').blur(function(event) {
		var pasd1 = $('.pasd_inp').val();
		// 将登录密码存进登录对象
		load_obj.objpasd = pasd1;
		load_judge(load_obj.objemail, load_obj.objpasd);
	});

	// 重置注册表单
	$('.reset_regs').click(function(event) {
		$('#regsrorm')[0].reset();
		$('.errregs').html('');
		$('.pasd_p').html('');
		$('.same').html('');
		$('.button_regs').attr('disabled', false);
		$('.button_regs').css('background-color', '#FF7F27');
	});
	// 重置登录表单
	$('.reset_load').click(function(event) {
		$('#loadform')[0].reset();
		$('.errmsg').html('');
	});

	$('.button_regs').click(function(event) {
		// 在用户名和密码都输入正确的情况下调用ajax
		console.log('zhong');
		$('.regspage').css('display', 'none');
		$('.waitpage').css('display', 'block');
		// $('.waitpage').delay(3000).hide(0);
		$(".waitpage").hide();
		var email = $('.regs_mail').val();
		var pasd = $('.same_pasd').val();
		register_ajax(email, pasd);

		console.log('hou');

	});

	$('.button_load').click(function(event) {
		if (load_obj.objemail != null && load_obj.objpasd != null) {
			load_ajax(load_obj.objemail, load_obj.objpasd);
		} else {

		}
	});
});

function search_jump(major, value) {
	if (major == "专业") {
		major = 1;
		window.open("search.html?t=" + major + "&c=" + value + "&#.");
	} else {
		major = 2;
		window.open("search_school.html?t=" + major + "&c=" + value + "&#.");
	}
}

var regs_obj = {
	objemail : 'null',
	objpasd : 'null'
};
var load_obj = {
	objemail : 'null',
	objpasd : 'null'
};

function register_ajax(email, pasd) {
	$.ajax({
		url : 'register.html',
		type : 'post',
		dataType : 'html',
		data : {
			"email" : email,
			"password" : pasd
		},
		success : function(msg) {
			data = eval('msg=' + msg);
			if (data.status == 0) {
				if (data.normalReturn.register == 'failure') {
					console.log('0');
					$('.regspage').css('display', 'block');
					$('.regsitem').css('border-bottom', '2px solid #FF7F27');
					$('.prompt').text(data.normalReturn.msg + '请重新注册').css(
							'color', 'red');
				} else {
					// 登录成功则向用户显示登录窗口
					$('.loadpage').css('display', 'block');
					$('.loaditem').css('border-bottom', '2px solid #428BCA');
					$('.regspage').css('display', 'none');
					$('.regsitem').css('border-bottom', 'none');
					$('.prompt').text('先去邮箱查看邮件激活吧!').css('color', 'red');
				}
			} else {
				// 404错误页面
				var err_msg = data.errMessage;
				if (err_msg !== '请先登录！') {
					$.cookie("err_msg", err_msg, {
						path : "/"
					});
					location.href = "error.html";
				} else {
					console.log(err_msg);
				}
			}
		}
	});
}

// 登录ajax
function load_ajax(email, pasd) {
	$.ajax({
		url : 'login',
		type : 'post',
		dataType : 'html',
		data : {
			"email" : email,
			"password" : pasd
		},
		success : function(msg) {
			data = eval('msg=' + msg);
			if (data.status == 0) {
				if (data.normalReturn.login == 'failure') {
					alert("登录失败");
				} else {
					$.cookie("user", data.normalReturn.code, {
						path : "/"
					});
					$('#modal_load').css('display', 'none');
					$('.user').css('display', 'block');
					$('.user2').css('display', 'none');
					window.location.reload();
				}
			} else {
				// 404错误页面
				var err_msg = data.errMessage;
				if (err_msg !== '请先登录！') {
					$.cookie("err_msg", err_msg, {
						path : "/"
					});
					location.href = "error.html";
				} else {
					console.log(err_msg);
				}
			}
		}
	});

}

function submit_judge(email,pasd)
{
	if(email!='null'&&pasd!='null'){
		console.log('qaz');
		$('.button_regs').attr('disabled', false);
		$('.button_regs').css('background-color', '#FF7F27');
//		submit_click(email,pasd);
		console.log('wer');
	}else{
		// 邮箱和密码若有一个错误择提交按钮不可用
//		$('.button_regs').click(function(event) {
			$('.button_regs').attr('disabled', true);
//			$('.button_regs').css('background-color', '#cbcbcb');
//		});

	}
}

function load_judge(email,pasd)
{
	if(email!='null'&&pasd!='null')
	{
//		console.log('true');
//		$('.button_load').attr('disabled', false);
//		$('.button_load').css('background-color', '#428BCA');
		// 点击登录按钮
//		load_submit(email,pasd);
	}else{
		$('.button_load').attr('disabled', true);
//		$('.button_load').css('background-color', '#cbcbcb');
	}
}