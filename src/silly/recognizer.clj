(ns silly.recognizer)

(defmacro with-new-recognizer [name & body]
  `(if js/window.webkitSpeechRecognition
     (let [~name (new js/webkitSpeechRecognition)]
       ~@body)
     (let [~name (new js/SpeechRecognition)]
       ~@body)))
