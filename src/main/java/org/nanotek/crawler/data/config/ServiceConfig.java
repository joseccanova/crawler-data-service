package org.nanotek.crawler.data.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nanotek.crawler.util.MapBeanTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Deprecated
public class ServiceConfig implements WebMvcConfigurer {

	public ServiceConfig() {
	}
	
	
	public void sqlMethod() {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT G.DS_GRP_PARAM AS DS_GRP_PARAM, P.* ");
		sqlQuery.append("	FROM PTM0032_PARAM_PRTAL P, PTM0031_GRP_PARAM_PRTAL G ");
		sqlQuery.append("		WHERE G.ID_GRP_PARAM_PRTAL = P.ID_GRP_PARAM_PRTAL ");
	}
	
	
	@Bean(name="restServlet")
	@Qualifier(value =  "restServlet")
	public InternalDispatcherServlet actionServlet() { 
		InternalDispatcherServlet actionServlet =  new InternalDispatcherServlet();
		return actionServlet;
	}
	
	@Bean(value="servletRegistrationBean")
	@Qualifier(value="servletRegistrationBean")
	public ServletRegistrationBean<InternalDispatcherServlet> servletRegistrationBean(@Autowired @Qualifier("restServlet") InternalDispatcherServlet actionServlet){
		ServletRegistrationBean<InternalDispatcherServlet> servletRegistrationBean  = new ServletRegistrationBean<InternalDispatcherServlet>(actionServlet,"/rest/**" ,"/");
		servletRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		servletRegistrationBean.setEnabled(true);
		servletRegistrationBean.setLoadOnStartup(0);
		return servletRegistrationBean;
	}

	
	class InternalDispatcherServlet extends DispatcherServlet{
		private static final long serialVersionUID = -5137340495580310650L;

		public InternalDispatcherServlet() {
			super();
		}
		
		public InternalDispatcherServlet(WebApplicationContext webApplicationContext) {
			super(webApplicationContext);
		}

		@Override
		protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
			System.out.println("DOING THE SERVICE");
			super.doService(request, response);
		}
	}
	
}
