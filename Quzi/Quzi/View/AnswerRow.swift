//
//  QuizRow.swift
//  Quzi
//
//  Created by Jinde Zou on 12/20/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct AnswerRow: View {
    
    let size = CGFloat(32)
    var quizItem: QuizItem
    var row: Int

    var body: some View {
        HStack {
            Text(MyUtil.trim(quizItem.array[row]))
            
            Spacer()
            
            if quizItem.multi_select {
                if quizItem.answer.contains("\(row)") {
                    Image("btn_check_on").frame(width: size, height: size)
                } else {
                    Image("btn_check_off").frame(width: size, height: size)
                }
            } else { // single select
                if quizItem.answer == "\(row)" {
                    Image("btn_radio_on").frame(width: size, height: size)
                } else {
                    Image("btn_radio_off").frame(width: size, height: size)
                }
            }
        }
    }
}

struct QuizRow_Previews: PreviewProvider {
    static var previews: some View {
        //AnswerRow(quizItem: "", rowStr: "My Answer")
        Text("test")
    }
}
