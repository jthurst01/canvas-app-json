/**
 * Copyright (c) 2013 salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package servlets;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.UserAgent;

import canvas.CanvasRequest;
import canvas.SignedRequest;

/**
 * Controller for canvas requests.
 * <p>
 * This class dispatches the request based on where the canvas application is
 * being rendered from.
 */
public class CanvasController extends HttpServlet {

	private static final String SIGNED_REQUEST_PARAM = "signed_request";

	private static final String DEFAULT_RESOURCE = "/default/index.jsp";
	private static final String NO_SIGNED_REQUEST_RESOURCE = "/welcome.jsp";

	private static final long serialVersionUID = 2956510495364791829L;

	@Override
	protected void service(HttpServletRequest request,
	        HttpServletResponse response) throws ServletException, IOException {
		String srString = request.getParameter(SIGNED_REQUEST_PARAM);

		request.setAttribute("ua",UserAgent.parse(request.getHeader("User-Agent")));
		if (!"POST".equals(request.getMethod())) {
			forward(NO_SIGNED_REQUEST_RESOURCE, request, response);
			return;
		}
		if (null == srString) {
			forward(NO_SIGNED_REQUEST_RESOURCE, request, response);
			return;
		}

		CanvasRequest cr = SignedRequest.verifyAndDecode(srString,
		        System.getenv("CANVAS_CONSUMER_SECRET"));
		request.setAttribute("canvasRequest", cr);
		request.setAttribute("canvasRequestJson", SignedRequest.toString(cr));

		String resource = String.format("/%s/index.jsp", cr.getContext()
		        .getEnvironmentContext().getDisplayLocation());
		forward(resource, request, response);
	}

	private void forward(String resource, HttpServletRequest request,
	        HttpServletResponse response) throws IOException, ServletException {
		boolean validResource = resourceExists(resource);

		RequestDispatcher dispatcher = validResource ? getServletContext()
		        .getRequestDispatcher(resource) : getServletContext()
		        .getRequestDispatcher(DEFAULT_RESOURCE);
		dispatcher.forward(request, response);
	}

	private boolean resourceExists(String path) throws MalformedURLException {
		return getServletContext().getResource(path) != null;
	}
}
