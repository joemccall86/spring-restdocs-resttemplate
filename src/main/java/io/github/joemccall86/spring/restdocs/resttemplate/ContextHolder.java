package io.github.joemccall86.spring.restdocs.resttemplate;

import org.springframework.restdocs.RestDocumentationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple holder class to keep track of the configuration and context for the
 * current rest template.
 *
 * NOTE this also implies that this extension is *not* thread safe, so parallel
 * test runners would break this strategy. We should possibly extend/wrap an
 * existing RestTemplate to attach these fields per-template.
 */
public class ContextHolder {
    public static Map<String, Object> configuration = new HashMap<>();
    public static RestDocumentationContext context = null;
}
