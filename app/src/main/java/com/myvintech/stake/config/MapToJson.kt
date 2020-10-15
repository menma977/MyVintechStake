package com.myvintech.stake.config

class MapToJson {
  fun map(hashMap: HashMap<String, String>): String {
    return hashMap.toString().replace(", ", "&").replace("{", "").replace("}", "")
  }
}