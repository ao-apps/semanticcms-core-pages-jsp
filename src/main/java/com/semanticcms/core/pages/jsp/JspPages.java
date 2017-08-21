/*
 * semanticcms-core-pages-jsp - SemanticCMS pages produced by JSP in the local servlet container.
 * Copyright (C) 2017  AO Industries, Inc.
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

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.net.Path;
import com.aoindustries.validation.ValidationException;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.pages.CaptureLevel;
import com.semanticcms.core.pages.Pages;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * Accesses JSP pages in the local {@link ServletContext}.
 */
public class JspPages implements Pages {

	private static final String INSTANCES_SERVLET_CONTEXT_KEY = JspPages.class.getName() + ".instances";

	/**
	 * Gets the JSP repository for the given context and prefix.
	 * Only one {@link JspPages} is created per unique context and prefix.
	 *
	 * @param  path  Must be a {@link Path valid path}.
	 *               Any trailing slash "/" will be stripped.
	 */
	public static JspPages getInstance(ServletContext servletContext, Path path) {
		// Strip trailing '/' to normalize
		{
			String pathStr = path.toString();
			if(!pathStr.equals("/") && pathStr.endsWith("/")) {
				try {
					path = Path.valueOf(
						pathStr.substring(0, pathStr.length() - 1)
					);
				} catch(ValidationException e) {
					AssertionError ae = new AssertionError("Stripping trailing slash from path should not render it invalid");
					ae.initCause(e);
					throw ae;
				}
			}
		}

		Map<Path,JspPages> instances;
		synchronized(servletContext) {
			@SuppressWarnings("unchecked")
			Map<Path,JspPages> map = (Map<Path,JspPages>)servletContext.getAttribute(INSTANCES_SERVLET_CONTEXT_KEY);
			if(map == null) {
				map = new HashMap<Path,JspPages>();
				servletContext.setAttribute(INSTANCES_SERVLET_CONTEXT_KEY, map);
			}
			instances = map;
		}
		synchronized(instances) {
			JspPages store = instances.get(path);
			if(store == null) {
				store = new JspPages(servletContext, path);
				instances.put(path, store);
			}
			return store;
		}
	}

	final ServletContext servletContext;
	final Path path;
	final String prefix;

	private JspPages(ServletContext servletContext, Path path) {
		this.servletContext = servletContext;
		this.path = path;
		String pathStr = path.toString();
		this.prefix = pathStr.equals("/") ? "" : pathStr;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * Gets the path, without any trailing slash except for "/".
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Gets the prefix useful for direct path concatenation, which is the path itself except empty string for "/".
	 */
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String toString() {
		return "jsp:" + prefix;
	}

	@Override
	public boolean exists(Path path) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Page getPage(Path path, CaptureLevel captureLevel) throws IOException, FileNotFoundException {
		throw new NotImplementedException();
	}
}
