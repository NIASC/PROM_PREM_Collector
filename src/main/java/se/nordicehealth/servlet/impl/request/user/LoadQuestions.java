package se.nordicehealth.servlet.impl.request.user;

import java.util.Map;
import java.util.Map.Entry;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.QuestionData;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class LoadQuestions extends RequestProcesser {
	private PPCDatabase db;
	
	public LoadQuestions(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.LOAD_Q);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = retrieveQuestions().toString();
		} catch (Exception e) { }
		data.put(Packet.QUESTIONS, result);

		out.put(Packet.DATA, data.toString());
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
			_question.put(Packet.OPTIONS, options.toString());
			_question.put(Packet.TYPE, _q.type);
			_question.put(Packet.ID, Integer.toString(_q.id));
			_question.put(Packet.QUESTION, _q.question);
			_question.put(Packet.DESCRIPTION, _q.description);
			_question.put(Packet.OPTIONAL, _q.optional ? Packet.YES : Packet.NO);
			_question.put(Packet.MAX_VAL, Integer.toString(_q.max_val));
			_question.put(Packet.MIN_VAL, Integer.toString(_q.min_val));
			
			_questions.put(_e.getKey(), _question.toString());
		}
		return _questions;
	}
}