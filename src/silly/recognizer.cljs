(ns silly.recognizer
  (:require-macros [silly.recognizer :refer [with-new-recognizer]])
  (:require [clojure.core.async :as a]))

(set! *warn-on-infer* true)

(defn make-recognizer [ch]
  (with-new-recognizer rec
    (set! (.-lang rec) "ja-JP")
    (set! (.-interimResults rec) false)
    (.addEventListener rec "result"
      (fn [^js/SpeechRecognitionEvent e]
        (let [^js/SpeechRecognitionAlternative alt (aget (.-results e) 0 0)]
          (a/put! ch (.-transcript alt)))))
    rec))
