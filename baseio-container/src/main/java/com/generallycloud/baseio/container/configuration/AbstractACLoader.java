/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.container.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.common.StringUtil;

public abstract class AbstractACLoader implements ApplicationConfigurationLoader{

	@Override
	public ApplicationConfiguration loadConfiguration(String rootPath,SharedBundle bundle) throws Exception {
		
		ApplicationConfiguration configuration = new ApplicationConfiguration();
		
		initApplicationConfigurationLoader(rootPath,configuration);

		configuration.setFiltersConfiguration(loadFiltersConfiguration(bundle));
		configuration.setPluginsConfiguration(loadPluginsConfiguration(bundle));
		configuration.setServletsConfiguration(loadServletsConfiguration(bundle));
		configuration.setPermissionConfiguration(loadPermissionConfiguration(bundle));
		
		return configuration;
	}
	
	protected abstract void initApplicationConfigurationLoader(String rootPath,ApplicationConfiguration configuration) throws IOException;
	
	protected abstract FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract ServicesConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle) throws IOException;
	
	protected FiltersConfiguration loadFiltersConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSON.parseArray(json);
		
		FiltersConfiguration configuration = new FiltersConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addFilters(c);
			
		}
		
		return configuration;
	}
	
	protected PluginsConfiguration loadPluginsConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSON.parseArray(json);
		
		PluginsConfiguration configuration = new PluginsConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addPlugins(c);
			
		}
		
		return configuration;
	}
	
	protected ServicesConfiguration loadServletsConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSON.parseArray(json);
		
		ServicesConfiguration configuration = new ServicesConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addServlets(c);
			
		}
		
		return configuration;
	}
}
