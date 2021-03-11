/*
 * semanticcms-core-pages-jsp - SemanticCMS pages produced by JSP in the local servlet container.
 * Copyright (C) 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-core-pages-jsp.
 *
 * semanticcms-core-pages-jsp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-core-pages-jsp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-core-pages-jsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.core.pages.jsp;

import com.aoindustries.net.Path;
import com.aoindustries.util.Tuple2;
import com.semanticcms.core.pages.local.LocalPageRepository;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Accesses JSP pages, with pattern *.jsp, in the local {@link ServletContext}.
 * Will not match *.inc.jsp.
 *
 * TODO: Block access to *.jspx in the page's local resources, block *.properties, too.
 */
public class JspPageRepository extends LocalPageRepository {

	@WebListener
	public static class Initializer implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent event) {
			getInstances(event.getServletContext());
		}
		@Override
		public void contextDestroyed(ServletContextEvent event) {
			// Do nothing
		}
	}

	private static final String INSTANCES_APPLICATION_ATTRIBUTE = JspPageRepository.class.getName() + ".instances";

	private static ConcurrentMap<Path, JspPageRepository> getInstances(ServletContext servletContext) {
		@SuppressWarnings("unchecked")
		ConcurrentMap<Path, JspPageRepository> instances = (ConcurrentMap<Path, JspPageRepository>)servletContext.getAttribute(INSTANCES_APPLICATION_ATTRIBUTE);
		if(instances == null) {
			instances = new ConcurrentHashMap<>();
			servletContext.setAttribute(INSTANCES_APPLICATION_ATTRIBUTE, instances);
		}
		return instances;
	}

	/**
	 * Gets the JSP repository for the given context and path.
	 * Only one {@link JspPageRepository} is created per unique context and path.
	 *
	 * @param  path  Must be a {@link Path valid path}.
	 *               Any trailing slash "/" will be stripped.
	 */
	public static JspPageRepository getInstance(ServletContext servletContext, Path path) {
		// Strip trailing '/' to normalize
		{
			String pathStr = path.toString();
			if(!pathStr.equals("/") && pathStr.endsWith("/")) {
				path = path.prefix(pathStr.length() - 1);
			}
		}

		ConcurrentMap<Path, JspPageRepository> instances = getInstances(servletContext);
		JspPageRepository repository = instances.get(path);
		if(repository == null) {
			repository = new JspPageRepository(servletContext, path);
			JspPageRepository existing = instances.putIfAbsent(path, repository);
			if(existing != null) repository = existing;
		}
		return repository;
	}

	private JspPageRepository(ServletContext servletContext, Path path) {
		super(servletContext, path);
	}

	@Override
	public String toString() {
		return "jsp:" + prefix;
	}

	@Override
	protected Tuple2<String, RequestDispatcher> getRequestDispatcher(Path path) throws IOException {
		String pathStr = path.toString();
		// Do not match *.inc.jsp
		if(pathStr.endsWith(".inc")) return null;
		String pathAdd = pathStr.endsWith("/") ? "index.jsp" : ".jsp";
		int len =
			prefix.length()
			+ pathStr.length()
			+ pathAdd.length();
		String resourcePath =
			new StringBuilder(len)
			.append(prefix)
			.append(pathStr)
			.append(pathAdd)
			.toString();
		assert resourcePath.length() == len;
		URL resource = cache.getResource(resourcePath);
		if(resource == null) return null;
		RequestDispatcher dispatcher = servletContext.getRequestDispatcher(resourcePath);
		if(dispatcher != null) {
			return new Tuple2<>(resourcePath, dispatcher);
		} else {
			return null;
		}
	}
}
