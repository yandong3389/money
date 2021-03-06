package d.money.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import d.money.common.utils.CollectionUtils;
import d.money.common.utils.Node;
import d.money.common.utils.NodeUtil;
import d.money.mapper.NodeExtMapper;
import d.money.mapper.base.MoneyHistoryMapper;
import d.money.mapper.base.NodeMapper;
import d.money.pojo.base.MoneyHistory;
import d.money.pojo.base.NodeExample;
import d.money.service.MoneyCalculateService;

@Service
public class MoneyCalculateServiceImpl implements MoneyCalculateService {
	
	@Autowired
	NodeExtMapper nodeExtMapper;
	@Autowired
	NodeMapper nodeMapper;
	@Autowired
	MoneyHistoryMapper moneyHistoryMapper;

	public int countByExample(NodeExample example) {
		return nodeMapper.countByExample(example);
	}
	
	/**
	 * 插入node数据
	 * @param userId 当前新增加的用户ID
	 * @param parentId 当前新增加的用户的接点人ID
	 */
	public void insertNode(int userId, int parentId){
		
		// 取得接点人节点
		d.money.pojo.base.Node parentNode = nodeMapper.selectByPrimaryKey(parentId);
		
		d.money.pojo.base.Node node = new d.money.pojo.base.Node();
		node.setCreateDate(new Date());
		node.setId(userId);
		node.setParentId(parentId);
		// 设置级别为接点人的下一级
		node.setLevel((parentNode.getLevel()+1));
		
		nodeMapper.insert(node);
	}
	
	/**
	 * 更新奖金数据,记录奖金获取历史
	 * @param nodeId 本次追加节点ID(当前新增加的用户ID)
	 * @param 介绍人ID(30%获取人)
	 */
	public void updateMoney (int nodeId, int jsrId) {
		
		// 取得所有节点数据
		List<Node> allNodeList = nodeExtMapper.selectByExample(null);
		
		NodeUtil.convertChild(allNodeList);
		NodeUtil.convertParent(allNodeList);
		
		// 推荐人
//		int node30Id = NodeUtil.getNodeById(allNodeList, nodeId).getParentId();
		
		// 直系人
		List<Node> node20List = this.get20Node(allNodeList, nodeId);

		// 旁系人(从最近的直系人开始往上算旁系)
		List<Node> node5List = this.get5NodeList(node20List.get(0));
		
		// 插入推荐奖金数据
		MoneyHistory history30 = new MoneyHistory();
		history30.setId(jsrId);
		// 1:推荐、2:直系、3:旁系
		history30.setType(1);
		history30.setCreateDate(new Date());
		moneyHistoryMapper.insert(history30);
		
		// 如果有直系节点
		if (CollectionUtils.isNotEmpty(node20List)) {
			
			for (Node node : node20List) {
				
				// 插入直系奖金数据
				MoneyHistory history20 = new MoneyHistory();
				history20.setId(node.getId());
				// 1:推荐、2:直系、3:旁系
				history20.setType(2);
				history20.setCreateDate(new Date());
				moneyHistoryMapper.insert(history20);
			}
		}
		
		// 有旁系节点
		if (CollectionUtils.isNotEmpty(node5List)) {
			
			// 循环插入旁系奖金数据
			for (Node node : node5List) {
				
				MoneyHistory history5 = new MoneyHistory();
				history5.setId(node.getId());
				// 1:推荐、2:直系、3:旁系
				history5.setType(3);
				history5.setCreateDate(new Date());
				moneyHistoryMapper.insert(history5);
			}
		}
	}
	
	/**
	 * 获取旁系节点集合
	 * @param node
	 * @return
	 */
	public List<Node> get5NodeList(Node node) {

		List<Node> nodes = new ArrayList<Node>();

		if (node != null) {
			
			while (node.getParentId() != 0) {
				nodes.add(node.getParentNode());
				node = node.getParentNode();
			}
		}

		return nodes;
	}

	/**
	 * 获取直系节点
	 * @param dataList
	 * @param id 本次追加节点ID
	 * @return
	 */
	public List<Node> get20Node(List<Node> dataList, int id) {

		Node thisNode = NodeUtil.getNodeById(dataList, id);

		List<Node> result = new ArrayList<Node>();
		
		get20Node(dataList, thisNode, thisNode.getLevel(), result);
		
		return result;
	}

	/**
	 * 递归计算符合配对节点
	 * @param dataList
	 * @param node
	 * @param level
	 * @return
	 */
	private void get20Node(List<Node> dataList, Node node, int level, List<Node> result) {

		Node parentNode = node.getParentNode();

		// 无节点符合要求
		if (parentNode == null || node.getParentId() == 0) {
			return;
		}

		List<Node> nodeChildren = parentNode.getChildren();

		if (nodeChildren.size() == 2) {

			int childNodesCount0 = new NodeUtil().getChildNodes(dataList,
					nodeChildren.get(0).getId(), level).size();
			int childNodesCount1 = new NodeUtil().getChildNodes(dataList,
					nodeChildren.get(1).getId(), level).size();

			if ((nodeChildren.get(0).getId() == node.getId()
					&& childNodesCount0 <= childNodesCount1 && childNodesCount1 > 0)
					|| (nodeChildren.get(1).getId() == node.getId()
							&& childNodesCount1 <= childNodesCount0 && childNodesCount0 > 0)) {
				result.add(node.getParentNode());
				get20Node(dataList, node.getParentNode(), level, result);
			} else {
				get20Node(dataList, node.getParentNode(), level, result);
			}

		} else if (nodeChildren.size() == 1) {
			get20Node(dataList, node.getParentNode(), level, result);
		}
	}

}
