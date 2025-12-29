package com.vvise.template.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controller that forwards SPA routes to index.html.
 * This allows React Router to handle client-side routing.
 *
 * All routes that don't match API endpoints or static files
 * will be forwarded to the React app's index.html.
 */
@Controller
class SpaController {

    /**
     * Forward all SPA routes to index.html.
     * React Router will handle the actual routing on the client side.
     *
     * Note: This must have lower priority than API routes and static resources.
     * Spring will try static resources first, then these routes.
     */
    @GetMapping(
        value = [
            "/",
            "/login",
            "/dashboard",
            "/dashboard/**",
            "/profile",
            "/profile/**",
            "/auth/callback"
        ]
    )
    fun forwardToIndex(): String {
        return "forward:/index.html"
    }
}
