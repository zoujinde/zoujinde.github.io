//
//  QuizView.swift
//  QuizApp
//
//  Created by Jinde Zou on 6/13/22.
//  Copyright © 2022 QuizApp. All rights reserved.
//

import SwiftUI
import Foundation

struct QuizView: View {

    // init
    init () {
        let gitUrl = URL(string: "https://raw.githubusercontent.com/zoujinde/zoujinde.github.io/main/server.txt")
        var request = URLRequest(url:gitUrl!)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let err = error {
                print("init error : \(err)")
                return
            }
            if let resp = response as? HTTPURLResponse {
                print("init response : \(resp.statusCode)")
            }
            if let data = data, let urlStr = String(data:data, encoding: .utf8) {
                print("init url : \(urlStr)")
                if let url = URL(string:urlStr)  {
                    UIApplication.shared.open(url, options:[:], completionHandler:(nil))
                }
            }
        }
        task.resume()
    }

    var body: some View {
        Text("Purple Sense App Is Loading ...")
    }
}
