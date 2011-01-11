# map-exception

Use maps as exceptions. This is a exploratory implementation of what an error handling library could look like. For the whole story, please see http://dev.clojure.org/display/design/Error+Handling

This lib is available on Clojars: [map-exception "1.0.0-SNAPSHOT"]

# Syntax Examples

    (use '[se.raek.map-exception :only (try+ throw-map try-multi try-multi-hierarchy)])
    
    (defn do-something []
      (println "in the finally clause"))
    
    (defn throwing-code []
      (throw-map {:type ::foo-error, :message "Invalid Foo"}))
    
    (try+
      (throwing-code)
      (catch ::foo-error m
        (str "got a foo error: " (:message m)))
      (catch ::bar-error m
        (str "got a bar error: " (:message m)))
      (catch RuntimeException e
        (str "got a runtime exception: " (.getMessage e)))
      (catch Exception e
        (str "got an exception: " (.getMessage e)))
      (finally
        (do-something)))
    
    ;; The above expands to:
    
    (try
      (try-map
        (throwing-code)
        (catch ::foo-error m
          (str "got a foo error: " (:message m)))
        (catch ::bar-error m
          (str "got a bar error: " (:message m))))
      (catch RuntimeException e
        (str "got a runtime exception: " (.getMessage e)))
      (catch Exception e
        (str "got an exception: " (.getMessage e)))
      (finally
        (do-something)))
    
    (defn other-throwing-code []
      (throw-map {:message "Some message.", :a 1, :b 2, :c 3}))
    
    (try-multi (juxt :a :b :c)
      (other-throwing-code)
      (catch [1 2 3] {:keys [message]}
        (str "one-two-three: " message))
      (catch [4 5 6] {:keys [message]}
        (str "four-five-six: " message))
      (finally
       (do-something)))

## License

Copyright (C) 2010 Rasmus Svensson

Distributed under the Eclipse Public License, the same as Clojure.
