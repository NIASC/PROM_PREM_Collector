package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import java.util.Map;
import java.util.Map.Entry;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.QuestionData;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class LoadQuestions extends RequestProcesser {
	
	public LoadQuestions(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.LOAD_Q);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = retrieveQuestions().toString();
		} catch (Exception e) { }
		data.put(Data.LoadQuestions.QUESTIONS, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData retrieveQuestions() throws Exception {
		Map<Integer, QuestionData> questions = db.loadQuestions();
		MapData _questions = packetData.getMapData();
		for (Entry<Integer, QuestionData> _e : questions.entrySet()) {
			QuestionData _q = _e.getValue();
			MapData _question = packetData.getMapData();
			ListData options = packetData.getListData();
			for (String str : _q.options) {
				options.add(str);
			}
			_question.put(Data.LoadQuestions.Question.OPTIONS, options.toString());
			_question.put(Data.LoadQuestions.Question.TYPE, _q.type);
			_question.put(Data.LoadQuestions.Question.ID, Integer.toString(_q.id));
			_question.put(Data.LoadQuestions.Question.QUESTION, _q.question);
			_question.put(Data.LoadQuestions.Question.DESCRIPTION, _q.description);
			_question.put(Data.LoadQuestions.Question.OPTIONAL,
					_q.optional ? Data.LoadQuestions.Question.Optional.YES : Data.LoadQuestions.Question.Optional.NO);
			_question.put(Data.LoadQuestions.Question.MAX_VAL, Integer.toString(_q.max_val));
			_question.put(Data.LoadQuestions.Question.MIN_VAL, Integer.toString(_q.min_val));
			
			_questions.put(_e.getKey(), _question.toString());
		}
		return _questions;
	}
}