//
//  QuizResult.swift
//  Quzi
//
//  Created by Jinde Zou on 12/21/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation

struct QuizResult: Codable {
    var quiz_id = 0
    var item_id = 0
    var user_id = 0
    var answer = ""

    //Because the client time maybe wrong
    //So server maybe refill the answer_time
    //var answer_time = ""
}
