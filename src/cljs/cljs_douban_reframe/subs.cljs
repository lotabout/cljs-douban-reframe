(ns cljs-douban-reframe.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

(re-frame/register-sub
 :channel-list
 (fn [db]
   (reaction (:channel-list @db))))

(re-frame/register-sub
 :current-channel-seq-id
 (fn [db]
   (reaction (:current-channel-seq-id @db))))

(re-frame/register-sub
 :current-song
 (fn [db]
   (reaction (:current-song @db))))
