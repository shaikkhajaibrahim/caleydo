package org.caleydo.view.tourguide.data.compute;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.view.tourguide.data.score.IComputedGroupScore;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class ComputeGroupScore implements Runnable {
	private static final Logger log = Logger.create(ComputeGroupScore.class);
	private final CachedIDTypeMapper mapper = new CachedIDTypeMapper();
	private final Table<Group, Pair<IDType, IDType>, Set<Integer>> sets = HashBasedTable.create();
	private final Multimap<TablePerspective, Group> data;
	private final Collection<IComputedGroupScore> scores;

	public ComputeGroupScore(Multimap<TablePerspective, Group> data, Collection<IComputedGroupScore> scores) {
		this.scores = scores;
		this.data = data;
	}


	@Override
	public void run() {
		if (data.isEmpty() || scores.isEmpty())
			return;

		// Multimap<IDType, TablePerspective> as = Grouper.byIDType(a.keySet());

		log.info("computing group similarity of " + data.size() + " against " + scores.size() + " others");
		Stopwatch w = new Stopwatch().start();

		for (TablePerspective as : this.data.keySet()) {
			final IDType aType = as.getRecordPerspective().getIdType();
			if (Thread.interrupted())
				return;
			for (IComputedGroupScore score : this.scores) {
				final IDType sType = score.getStratification().getRecordPerspective().getIdType();

				IDType target = score.getTargetType(as);
				for (Group ag : this.data.get(as)) {
					if (Thread.interrupted())
						return;
					if (score.contains(as, ag))
						continue;
					Set<Integer> aids = get(as, ag, target, sType);
					Set<Integer> sids = get(score.getStratification(), score.getGroup(), target, aType);
					float v = score.compute(aids, sids);
					score.put(ag, v);
				}
			}
			// cleanup cache
			for (Group targetGroup : data.get(as)) {
				sets.row(targetGroup).clear();
			}
		}
		System.out.println("done in " + w);
	}

	private Set<Integer> get(TablePerspective strat, Group group, IDType target, IDType occurIn) {
		Pair<IDType, IDType> check = Pair.make(target, occurIn);
		if (sets.contains(group, check))
			return sets.get(group, check);

		IDType source = strat.getRecordPerspective().getIdType();

		IIDTypeMapper<Integer, Integer> mapper = this.mapper.get(source, target);
		Set<Integer> r = null;
		if (mapper == null)
			r = Collections.emptySet();
		else {
			RecordVirtualArray va = strat.getRecordPerspective().getVirtualArray();
			r = mapper.apply(va.getIDsOfGroup(group.getGroupIndex()));
			if (!target.equals(occurIn) && !source.equals(occurIn)) { // check against third party
				Predicate<Integer> in = this.mapper.in(target, occurIn);
				// filter the wrong out
				for (Iterator<Integer> it = r.iterator(); it.hasNext();)
					if (!in.apply(it.next()))
						it.remove();
			}
		}
		sets.put(group, check, r);
		return r;
	}
}