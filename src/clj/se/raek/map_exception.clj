(ns se.raek.map-exception
  (:import clojure.lang.IPersistentMap
           se.raek.map_exception.PersistentMapException))



(declare analyze-try+-body analyze-try-map-body
         try+-error-message try-map-error-message try-multi-error-message
         expr? catch-clause? host-type?
         map-catch-clause? host-catch-clause? finally-clause?)

(defmacro try-map
  {:arglists ['(expr* map-catch-clause* host-catch-claus* finally-clause?)]}
  [& body]
  (if-let [[exprs map-catch-clauses finally-clause]
           (analyze-try-map-body body)]
    `(try
       ~@exprs
       ~@(when (seq map-catch-clauses)
           (let [map-symbol (gensym)]
             [`(catch PersistentMapException e#
                 (let [~map-symbol (.getMap e#)]
                   (condp #(isa? %2 %1) (:type ~map-symbol)
                     ~@(mapcat
                        (fn [[_ type binding-form & clause-body]]
                          [type `(let [~binding-form ~map-symbol]
                                   ~@clause-body)])
                        map-catch-clauses)
                     (throw e#))))]))
       ~@finally-clause)
    (throw (Exception. try-map-error-message))))

(defmacro try+
  {:arglists ['(expr* map-catch-clause* host-catch-claus* finally-clause?)]}
  [& body]
  (if-let [[exprs map-catch-clauses host-catch-clauses finally-clause]
           (analyze-try+-body body)]
    `(try
       (try-map
         ~@exprs
         ~@map-catch-clauses)
       ~@host-catch-clauses
       ~@finally-clause)
    (throw (Exception. try+-error-message))))

(defmacro try-multi
  {:arglists ['(dispatch-fn expr* catch-clause* finaly-clause?)]}
  [dispatch-fn & body]
  (if-let [[exprs catch-clauses finally-clause]
           (analyze-try-map-body body)]
    `(try
       ~@exprs
       ~@(when (seq catch-clauses)
           (let [map-symbol (gensym)]
             [`(catch PersistentMapException e#
                 (let [~map-symbol (.getMap e#)]
                   (condp #(isa? %2 %1) (~dispatch-fn ~map-symbol)
                     ~@(mapcat
                        (fn [[_ type binding-form & clause-body]]
                          [type `(let [~binding-form ~map-symbol]
                                   ~@clause-body)])
                        catch-clauses)
                     (throw e#))))]))
       ~@finally-clause)
    (throw (Exception. try-multi-error-message))))

(defmacro try-multi-hierarchy
  {:arglists ['(dispatch-fn hierarchy expr* catch-clause* finaly-clause?)]}
  [dispatch-fn hierarchy & body]
  (if-let [[exprs catch-clauses finally-clause]
           (analyze-try-map-body body)]
    `(try
       ~@exprs
       ~@(when (seq catch-clauses)
           (let [map-symbol (gensym)]
             [`(catch PersistentMapException e#
                 (let [~map-symbol (.getMap e#)]
                   (condp #(isa? ~hierarchy %2 %1) (~dispatch-fn ~map-symbol)
                     ~@(mapcat
                        (fn [[_ type binding-form & clause-body]]
                          [type `(let [~binding-form ~map-symbol]
                                   ~@clause-body)])
                        catch-clauses)
                     (throw e#))))]))
       ~@finally-clause)
    (throw (Exception. try-multi-error-message))))

(defn- analyze-try+-body [body]
  (let [[exprs              body2] (split-with expr? body)
        [map-catch-clauses  body3] (split-with map-catch-clause? body2)
        [host-catch-clauses body4] (split-with host-catch-clause? body3)
        [finally-clauses    body5] (split-with finally-clause? body4)]
    (when-not (or (seq body5) (next finally-clauses))
      [exprs map-catch-clauses host-catch-clauses finally-clauses])))

(defn- analyze-try-map-body [body]
  (let [[exprs           body2] (split-with expr? body)
        [catch-clauses   body3] (split-with catch-clause? body2)
        [finally-clauses body4] (split-with finally-clause? body3)]
    (when-not (or (seq body4) (next finally-clauses))
      [exprs catch-clauses finally-clauses])))

(def ^{:private true} try+-error-message
  "A try+ form must follow the pattern (try+ expr* map-catch-clause* host-catch-clause* finally-clause?)")

(def ^{:private true} try-map-error-message
  "A try-map form must follow the pattern (try+ expr* map-catch-clause* finally-clause?)")

(def ^{:private true} try-multi-error-message
  "A try-multi form must follow the pattern (try-multi dispatch-fn expr* catch-clause* finally-clause?)")

(def ^{:private true} try-multi-hierarchy-error-message
  "A try-multi-hierarchy form must follow the pattern (try-multi-hierarchy dispatch-fn expr* catch-clause* finally-clause?)")

(defn- expr? [form]
  (not (and (seq? form)
            (#{'catch 'finally} (first form)))))

(defn- catch-clause? [form]
  (and (seq? form)
       (= (first form) 'catch)))

(defn- host-type? [type]
  (cond (class? type)  true
        (symbol? type) (class? (resolve type))
        :else          false))

(defn- host-catch-clause? [form]
  (and (catch-clause? form)
       (host-type? (second form))))

(defn- map-catch-clause? [form]
  (and (catch-clause? form)
       (not (host-type? (second form)))))

(defn- finally-clause? [form]
  (and (seq? form)
       (= (first form) 'finally)))



(declare wrap-map-in-exception)

(defmacro throw+ [throwable-or-map]
  `(throw (wrap-map-in-exception ~throwable-or-map)))

(defmacro throw-map [m]
  `(throw (PersistentMapException. ~m)))

(defn map-exception [m]
  (PersistentMapException. m))

(definline wrap-map-in-exception [throwable-or-map]
  `(let [throwable-or-map# ~throwable-or-map]
     (condp instance? throwable-or-map#
       Throwable throwable-or-map#
       IPersistentMap (PersistentMapException. throwable-or-map#)
       (throw (IllegalArgumentException.
               "Argument must be a Throwable or an IPersistentMap")))))

