(ns silly.utils
  (:require [clojure.walk :as walk]))

(defmacro with-available-ctor [name ctors & body]
  `(cond
     ~@(mapcat (fn [ctor]
                 [`(cljs.core/exists? ~ctor)
                  `(do ~@(walk/postwalk-replace {name ctor} body))])
               ctors)))

(comment

  (with-available-ctor ctor [js/webkitSpeechRecognition
                             js/SpeechRecognition]
    (let [rec (new ctor)]
      (set! (.-lang rec) "ja-JP")))

  ; will be expanded to something like the following

  (cond (exists? js/webkitSpeechRecognition)
        (let [rec (new js/webkitSpeechRecognition)]
          (set! (.-lang rec) "ja-JP"))

        (exists? js/SpeechRecognition)
        (let [rec (new js/SpeechRecognition)]
          (set! (.-lang rec) "ja-JP")))

)
