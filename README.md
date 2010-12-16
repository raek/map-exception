# map-exception

Use maps as exceptions. See this thread for background: http://groups.google.com/group/clojure-dev/browse_thread/thread/734ee59f6cbc1b55/5f975739698fbb8c

This lib is available on Clojars: [map-exception "1.0.0-SNAPSHOT"]

Syntax examples:

  (use '[se.raek.map-exception :only (try+ throw+ try-multi try-multi-hierarchy)])
  
  (defn do-something []
    (println "in the finally clause"))
  
  (defn throwing-code []
    (throw+ {:type ::foo-error, :message "Invalid Foo"}))
  
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
  
  (defn other-throwing-code []
    (throw+ {:message "Some message.", :a 1, :b 2, :c 3}))
  
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