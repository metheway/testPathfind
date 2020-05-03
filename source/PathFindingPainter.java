import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;

public class PathFindingPainter {

	private final MapReader mapReader;
	//FRAME
	JFrame frame;
	//GENERAL VARIABLES
	private int cells = 20;
	private int delay = 30;
	private double dense = .5;
	private double density = (cells*cells)*.5;
	public int startx = -1;
	public int starty = -1;
	public int finishx = -1;
	public int finishy = -1;
	private NodeType tool = NodeType.START;
	private int checks = 0;
	private int length = 0;
	private int curAlg = 0;
	private int WIDTH = 850;
	private final int HEIGHT = 650;
	private final int MSIZE = 600;
	private int CSIZE = MSIZE/cells;
	//UTIL ARRAYS
	private String[] algorithms = {"A*", "A_fastreopen", "WA_noreopen", "WA_fastReopen",
	"XDP_noreopen", "XDP_fastreopen", "RTAA", "RTA_XDP", "RTA_XUP", "RAA", "RAA_XDP", "RAA_XUP"};
	private String[] tools = {"Start","Finish","Wall", "Eraser", "checked"};
	//BOOLEANS
	private boolean solving = false;
	//UTIL
	Node[][] map;
	Algorithm Alg = new Algorithm();
	Random r = new Random();
	//SLIDERS
	JSlider size = new JSlider(1,5,2);
	JSlider speed = new JSlider(0,500,delay);
	JSlider obstacles = new JSlider(1,100,50);
	//LABELS
	JLabel algL = new JLabel("Algorithms");
	JLabel toolL = new JLabel("Toolbox");
	JLabel sizeL = new JLabel("Size:");
	JLabel cellsL = new JLabel(cells+"x"+cells);
	JLabel delayL = new JLabel("Delay:");
	JLabel msL = new JLabel(delay+"ms");
	JLabel obstacleL = new JLabel("Dens:");
	JLabel densityL = new JLabel(obstacles.getValue()+"%");
	JLabel checkL = new JLabel("Checks: "+checks);
	JLabel lengthL = new JLabel("Path Length: "+length);
	//BUTTONS
	JButton searchB = new JButton("Start Search");
	JButton resetB = new JButton("Reset");
	JButton genMapB = new JButton("Generate Map");
	JButton clearMapB = new JButton("Clear Map");
	JButton creditB = new JButton("Credit");
	//DROP DOWN
	JComboBox algorithmsBx = new JComboBox(algorithms);
	JComboBox toolBx = new JComboBox(tools);
	//PANELS
	JPanel toolP = new JPanel();
	//CANVAS
	Map canvas;
	//BORDER
	Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	public static void main(String[] args) {	//MAIN METHOD
		MapReader mapReader = new MapReader();
		PathFindingPainter pathFindingPainter = new PathFindingPainter(mapReader);
		// 读取地图
	}

	public PathFindingPainter(MapReader mapReader) {	//CONSTRUCTOR
		this.mapReader = mapReader;
		clearMap();
		density = (cells*cells)*dense;
		CSIZE = MSIZE/cells;
		// 初始化map
		initialize();
		Update();
	}
	
	public void generateMap() {	//GENERATE MAP
		clearMap();	//CREATE CLEAR MAP TO START
		for(int i = 0; i < density; i++) {
			Node current;
			do {
				int x = r.nextInt(cells);
				int y = r.nextInt(cells);
				current = map[x][y];	//FIND A RANDOM NODE IN THE GRID
			} while(current.getType()== NodeType.WALL);	//IF IT IS ALREADY A WALL, FIND A NEW ONE
			current.setType(NodeType.WALL);	//SET NODE TO BE A WALL
		}
	}
	
	public void clearMap() {	//CLEAR MAP
		finishx = -1;	//RESET THE START AND FINISH
		finishy = -1;
		startx = -1;
		starty = -1;

		try {
			mapReader.readMaps();
			mapReader.init();
			this.map = mapReader.currentMap;
			this.cells = map.length;
		} catch (IOException e) {
			e.printStackTrace();
		}
		map = mapReader.currentMap;

		mapReader.start.setHops(0);
		startx = mapReader.start.getRow();	//SET THE START X AND Y
		starty = mapReader.start.getCol();
		mapReader.end.setHops(0);
		finishx = mapReader.end.getRow();	//SET THE START X AND Y
		finishy = mapReader.end.getCol();

//		map = new Node[cells][cells];	//CREATE NEW MAP OF NODES
//		for(int x = 0; x < cells; x++) {
//			for(int y = 0; y < cells; y++) {
//				map[x][y] = new Node(NodeType.EMPTY,x,y);	//SET ALL NODES TO EMPTY
//			}
//		}

		reset();	//RESET SOME VARIABLES
	}
	
	public void resetMap() {	//RESET MAP
		Alg.running = true;

		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				Node current = map[x][y];
				if(current.getType() == NodeType.CHECKED || current.getType() == NodeType.FINALPATH)
					//CHECK TO SEE IF CURRENT NODE IS EITHER CHECKED OR FINAL PATH
					map[x][y] = new Node(NodeType.EMPTY,x,y);	//RESET IT TO AN EMPTY NODE
			}
		}
		if(startx > -1 && starty > -1) {	//RESET THE START AND FINISH
			map[startx][starty] = new Node(NodeType.START,startx,starty);
			map[startx][starty].setHops(0);
		}
		if(finishx > -1 && finishy > -1)
			map[finishx][finishy] = new Node(NodeType.FINISH,finishx,finishy);
			reset();	//RESET SOME VARIABLES
	}

	private void initialize() {	//INITIALIZE THE GUI ELEMENTS
		frame = new JFrame();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(WIDTH,HEIGHT);
		frame.setTitle("Path Finding");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		toolP.setBorder(BorderFactory.createTitledBorder(loweredetched,"Controls"));
		int space = 25;
		int buff = 45;
		
		toolP.setLayout(null);
		toolP.setBounds(10,10,210,600);
		
		searchB.setBounds(40,space, 120, 25);
		toolP.add(searchB);
		space+=buff;
		
		resetB.setBounds(40,space,120,25);
		toolP.add(resetB);
		space+=buff;
		
		genMapB.setBounds(40,space, 120, 25);
		toolP.add(genMapB);
		space+=buff;
		
		clearMapB.setBounds(40,space, 120, 25);
		toolP.add(clearMapB);
		space+=40;
		
		algL.setBounds(40,space,120,25);
		toolP.add(algL);
		space+=25;
		
		algorithmsBx.setBounds(40,space, 120, 25);
		toolP.add(algorithmsBx);
		space+=40;
		
		toolL.setBounds(40,space,120,25);
		toolP.add(toolL);
		space+=25;
		
		toolBx.setBounds(40,space,120,25);
		toolP.add(toolBx);
		space+=buff;
		
		sizeL.setBounds(15,space,40,25);
		toolP.add(sizeL);
		size.setMajorTickSpacing(10);
		size.setBounds(50,space,100,25);
		toolP.add(size);
		cellsL.setBounds(160,space,40,25);
		toolP.add(cellsL);
		space+=buff;
		
		delayL.setBounds(15,space,50,25);
		toolP.add(delayL);
		speed.setMajorTickSpacing(5);
		speed.setBounds(50,space,100,25);
		toolP.add(speed);
		msL.setBounds(160,space,40,25);
		toolP.add(msL);
		space+=buff;
		
		obstacleL.setBounds(15,space,100,25);
		toolP.add(obstacleL);
		obstacles.setMajorTickSpacing(5);
		obstacles.setBounds(50,space,100,25);
		toolP.add(obstacles);
		densityL.setBounds(160,space,100,25);
		toolP.add(densityL);
		space+=buff;
		
		checkL.setBounds(15,space,100,25);
		toolP.add(checkL);
		space+=buff;
		
		lengthL.setBounds(15,space,100,25);
		toolP.add(lengthL);
		space+=buff;
		
		creditB.setBounds(40, space, 120, 25);
		toolP.add(creditB);
		
		frame.getContentPane().add(toolP);
		
		canvas = new Map();
		canvas.setBounds(230, 10, MSIZE+1, MSIZE+1);
		frame.getContentPane().add(canvas);
		
		searchB.addActionListener(new ActionListener() {		//ACTION LISTENERS
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				if((startx > -1 && starty > -1) && (finishx > -1 && finishy > -1))
					solving = true;
				startSearch();	//START STATE

			}
		});
		resetB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 首先要停止算法
				resetMap();
				Update();
			}
		});
		genMapB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateMap();
				Update();
			}
		});
		clearMapB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearMap();
				Update();
			}
		});
		algorithmsBx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				curAlg = algorithmsBx.getSelectedIndex();
				Update();
			}
		});
		toolBx.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int t = toolBx.getSelectedIndex();
				switch (t) {
					case 0:
						tool = NodeType.START;
						break;
					case 1:
						tool = NodeType.FINISH;
						break;
					case 2:
						tool = NodeType.WALL;
						break;
					case 3:
						tool = NodeType.EMPTY;
						break;
					case 4:
						tool = NodeType.CHECKED;
						break;
					default:
						break;
				}
				Update();
			}
		});
		size.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cells = size.getValue()*10;
				clearMap();
				reset();
				Update();
			}
		});
		speed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				delay = speed.getValue();
				Update();
			}
		});
		obstacles.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				dense = (double)obstacles.getValue()/100;
				Update();
			}
		});
		creditB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "	                         Pathfinding\n"
												   + "             Copyright (c) 2017-2018\n"
												   + "                         Greer Viau\n"
												   + "          Build Date:  March 28, 2018   ", "Credit", JOptionPane.PLAIN_MESSAGE, new ImageIcon(""));
			}
		});

//		startSearch();	//START STATE
	}
	
	public void startSearch() {	//START STATE
		solving = true;

		if(solving) {
			try {
				switch(curAlg) {
					case 0:
						Alg.AStar(this);
						break;
					case 1:
						Alg.AStarFast(this);
						break;
					case 2:
						Alg.WA(this);
						break;
					case 3:
						Alg.WAFast(this);
						break;
					case 4:
						Alg.XDP(this);
						break;
					case 5:
						Alg.XDPFast(this);
						break;
					case 6:
						Alg.RTAA(this);
						break;
					case 7:
						Alg.RTA_XDP(this);
						break;
					case 8:
						Alg.RTA_XUP(this);
						break;
					case 9:
						Alg.RAA(this);
					case 10:
						Alg.RAA_XDP(this);
					case 11:
						Alg.RAA_XUP(this);
					default:
						break;
				}
			}catch (Exception e) {}
		}
//		pause();	//PAUSE STATE
	}
	
	public void pause() {	//PAUSE STATE
		int i = 0;
		while(!solving) {
			// 如果解决完了，暂停
			i++;
			if(i > 500)
				i = 0;
			try {
				Thread.sleep(1);
			} catch(Exception e) {}
		}
		startSearch();	//START STATE
	}
	
	public void Update() {	//UPDATE ELEMENTS OF THE GUI
		density = (cells*cells)*dense;
		CSIZE = MSIZE/cells;
		canvas.repaint();
		cellsL.setText(cells+"x"+cells);
		msL.setText(delay+"ms");
		lengthL.setText("Path Length: "+length);
		densityL.setText(obstacles.getValue()+"%");
		checkL.setText("Checks: "+checks);
	}
	
	public void reset() {	//RESET METHOD
		solving = false;
		length = 0;
		checks = 0;
	}
	
	public void delay() {	//DELAY METHOD
		try {
			Thread.sleep(delay);
		} catch(Exception e) {}
	}

	public boolean getHops() {
		return false;
	}

	public int getType() {
		return 0;
	}

	public void onUpdate() {
		// 更新节点并且
		Update();
		solving = false;
		delay();
	}

	class Map extends JPanel implements MouseListener, MouseMotionListener{	//MAP CLASS
		
		public Map() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void paintComponent(Graphics g) {	//REPAINT
			super.paintComponent(g);
			for(int x = 0; x < cells; x++) {	//PAINT EACH NODE IN THE GRID
				for(int y = 0; y < cells; y++) {
					switch(map[x][y].getType()) {
						case START:
							g.setColor(Color.GREEN);
							break;
						case FINISH:
							g.setColor(Color.RED);
							break;
						case WALL:
							g.setColor(Color.BLACK);
							break;
						case EMPTY:
							g.setColor(Color.WHITE);
							break;
						case CHECKED:
							g.setColor(Color.CYAN);
							break;
						case FINALPATH:
							g.setColor(Color.YELLOW);
							break;
						case OPEN:
							g.setColor(Color.LIGHT_GRAY);
							break;
						default:break;
					}
					g.fillRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
//					g.setColor(Color.BLACK);
//					g.drawRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				int x = e.getX()/CSIZE;	
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				if((tool == NodeType.WALL  || tool == NodeType.EMPTY) &&
						(current.getType() != NodeType.START && current.getType() != NodeType.FINISH))
					current.setType(tool);
				Update();
			} catch(Exception z) {}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			resetMap();	//RESET THE MAP WHENEVER CLICKED
			try {
				int x = e.getX()/CSIZE;	//GET THE X AND Y OF THE MOUSE CLICK IN RELATION TO THE SIZE OF THE GRID
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				switch(tool) {
					case START: { //START NODE
						if(current.getType() != NodeType.WALL) {	//IF NOT WALL
							if(startx > -1 && starty > -1) {	//IF START EXISTS SET IT TO EMPTY
								map[startx][starty].setType(NodeType.EMPTY);
								map[startx][starty].setHops(-1);
							}
							current.setHops(0);
							startx = x;	//SET THE START X AND Y
							starty = y;
							current.setType(NodeType.START);	//SET THE NODE CLICKED TO BE START
						}
						break;
					}
					case FINISH: { //FINISH NODE
						if(current.getType()!=NodeType.WALL) {	//IF NOT WALL
							if(finishx > -1 && finishy > -1)	//IF FINISH EXISTS SET IT TO EMPTY
								map[finishx][finishy].setType(NodeType.EMPTY);
							finishx = x;	//SET THE FINISH X AND Y
							finishy = y;
							current.setType(NodeType.FINISH);	//SET THE NODE CLICKED TO BE FINISH
						}
						break;
					}
					case CHECKED: {
						if (current.getType() != NodeType.WALL) {    //IF NOT WALL
							if (finishx > -1 && finishy > -1)    //IF FINISH EXISTS SET IT TO EMPTY
								map[finishx][finishy].setType(NodeType.EMPTY);
							finishx = x;    //SET THE FINISH X AND Y
							finishy = y;
							current.setType(NodeType.CHECKED);    //SET THE NODE CLICKED TO BE FINISH
							break;
						}
					}
					default:
						if(current.getType() != NodeType.START && current.getType() != NodeType.FINISH) {
							// 如果不是起点或者终点，设置为障碍
							current.setType(NodeType.WALL);
						}
						break;
				}
				Update();
			} catch(Exception z) {}	//EXCEPTION HANDLER
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}
}
