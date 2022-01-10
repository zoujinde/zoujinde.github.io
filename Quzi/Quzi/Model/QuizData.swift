//
//  QuizData.swift
//  Quzi
//
//  Created by Jinde Zou on 12/19/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation

// Quiz Data
final class QuizData {
    //let quiz_list: [QuizItem] = MyUtil.load(file: "quiz_data.json")
    static func getQuizList(quiz_id: Int, user_id: Int) -> [QuizItem] {
        let url = MyUtil.HTTP_URL + "/data?tab=quiz_result"
        let body = "{\"act\":\"select\", \"quiz_id\":\(quiz_id), \"user_id\":\(user_id)}"
        let data = MyUtil.httpPost(urlStr: url, body: body)
        var list: [QuizItem] = MyUtil.load(data: data)
        var i = 0
        while i < list.count {
            list[i].answer_old = list[i].answer
            list[i].array = list[i].item_answer.components(separatedBy: " # ")
            i+=1
        }
        return list
    }
}

// Quiz Item
struct QuizItem: Codable {
    let quiz_id: Int
    let item_id: Int
    let item_content: String
    let item_answer: String
    var array: [String]?
    let multi_select: Bool
    var answer: String
    var answer_old: String? // Must define the optional, because http data no the answer_old
}

