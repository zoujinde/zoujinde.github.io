//
//  QuizData.swift
//  Quzi
//
//  Created by Jinde Zou on 12/19/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation

// Quiz Data
final class QuizList {
    let quiz_list: [QuizItem] = MyUtil.load(file: "quiz_data.json")
}

// Quiz Item
struct QuizItem: Codable {
    let quiz_id: Int
    let item_id: Int
    let item_content: String
    let item_answers: String
    var array: [String] {
        return item_answers.split(separator: "\n").map{String($0)}
    }
    let multi_select: Bool
    var answer: String
}

