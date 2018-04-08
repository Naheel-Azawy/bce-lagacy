package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TextAction;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import app.Info;
import app.Updater;
import computer.ComputerAbstract;
import utils.Format;
import utils.Logger;
import utils.Utils;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Color LIGHT = new Color(0xE8E8E7);
	private static final Color DARK = new Color(0x252A2C);
	private static final Color DARKER = new Color(0x1E1E1E);
	private static final String FONT_MONO = "Courier New";
	private static final String FONT_NORMAL = "Arial";
	private static final int TXT_SIZE = 18;
	private static final int EDITOR_TXT_SIZE = 20;
	private static final int W = 1500, H = 1000;
	private static final String[] P_TYPES = { "Assembly", "Hexadecimal", "Decimal", "Binary" };
	private static final String KEY_SCALE = "scale";
	private static final String KEY_LIGHT = "is_light";
	private static final int MEM_LEN = 20;
	private static final int MEM_LINES = MEM_LEN + 5;

	private float scale = 1f;
	private int w, h;
	private Color bgColor;
	private Color bgColor2;
	private Color txtColor;
	private int txtSize, editorTxtSize;
	private boolean isLight = false;
	private Preferences prefs;

	private ComputerAbstract c;
	private Logger logger;
	private short[] M;
	private short AR, PC, DR, AC, IR, TR, INPR, OUTR;
	private byte SC;
	private boolean S, E, R, IEN, FGI, FGO;
	private int mStart;

	private JComboBox<String> pType = new JComboBox<>(P_TYPES);
	private Txts t;
	private TxtF scaleF;

	public MainFrame(ComputerAbstract c) {
		this.c = c;
		this.logger = c.getLogger();
		this.prefs = Preferences.userNodeForPackage(MainFrame.class);
		this.scale = prefs.getFloat(KEY_SCALE, 1f);
		this.isLight = prefs.getBoolean(KEY_LIGHT, false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				prefs.putFloat(KEY_SCALE, scale);
				prefs.putBoolean(KEY_LIGHT, isLight);
				System.exit(0);
			}
		});
		setTitle(Info.NAME);
		setResizable(true);

		final Btn open, save, saveAs, run, tick, hlt, clr, thm, update, about;
		final TxtF freqF, freqAvgF;

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JPanel top = new JPanel(new BorderLayout());
		JPanel topL = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel topR = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(topL, BorderLayout.WEST);
		top.add(topR, BorderLayout.EAST);
		topL.add(open = new Btn("Open"));
		topL.add(save = new Btn("Save"));
		topL.add(saveAs = new Btn("Save as"));
		topL.add(run = new Btn("Run"));
		topL.add(tick = new Btn("Tick"));
		topL.add(hlt = new Btn("Halt"));
		topL.add(clr = new Btn("Clear"));
		topR.add(new Lbl("Avg Freq (Hz): "));
		topR.add(freqAvgF = new TxtF(getFreq(), 4));
		topR.add(new Lbl("Freq (Hz): "));
		topR.add(freqF = new TxtF(getFreq(), 4));
		topR.add(new Lbl("Scaling: "));
		topR.add(scaleF = new TxtF(String.valueOf(scale), 3));
		topR.add(thm = new Btn("Switch theme"));
		topR.add(update = new Btn("Update"));
		topR.add(about = new Btn("About"));

		p.add(top, BorderLayout.NORTH);
		p.add((t = new Txts()).getPane(), BorderLayout.CENTER);

		addTheme(() -> {
			top.setBackground(bgColor2);
			topL.setBackground(bgColor2);
			topR.setBackground(bgColor2);
		});
		freqAvgF.setEditable(false);

		open.addActionListener(e -> {
			try {
				JFileChooser chooser = new JFileChooser(Utils.getDir());
				int returnVal = chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String path = chooser.getSelectedFile().getAbsolutePath();
					loadProgram(t, path);
					t.setSrc(c.getSource());
				}
			} catch (Exception ignored) {
			}
		});
		save.addActionListener(e -> saveProgram(c.getSourcePath()));
		saveAs.addActionListener(e -> saveProgram(null));
		run.addActionListener(e -> {
			if (loadProgram(t, null)) {
				setFreq(freqF.getText());
				c.clearReg();
				c.clearIO();
				c.startAsync();
			}
		});
		tick.addActionListener(e -> {
			c.startEnable();
			c.tickAsync();
		});
		hlt.addActionListener(e -> c.stop());
		clr.addActionListener(e -> {
			c.stop();
			c.clear();
			logger.clear();
			c.clearIO();
		});
		thm.addActionListener(e -> this.toggleTheme());
		scaleF.addActionListener(e -> {
			try {
				scale = Float.parseFloat(scaleF.getText());
				updateSize();
			} catch (Exception ignored) {
			}
		});
		about.addActionListener(e -> {
			final JDialog d = new JDialog(this);
			showMessageDialog(d, Info.ABOUT, "About", JOptionPane.PLAIN_MESSAGE, new ImageIcon(Info.ICON),
					new Btn("OK", e1 -> d.dispose()));
		});
		update.addActionListener(e -> {
			progress(this, () -> Updater.getLatestVersion(), latestVersion -> {
				final JDialog d = new JDialog(this);
			    if (latestVersion < 0) {
			        showMessageDialog(d, "Checking for updates failed!", "Error!", JOptionPane.PLAIN_MESSAGE, null,
			            new Btn("OK", e1 -> {
							d.dispose();
						}));
					return;
			    }
				Runnable install = () -> progress(this, () -> Updater.download(), fName -> Updater.run(fName));
				String msg;
				if (latestVersion > Double.parseDouble(Info.VERSION)) {
					msg = "Would like to update to the latest version (" + latestVersion + ")?";
				} else {
					msg = "<html>Already up to date at version (" + latestVersion + ")!<br>Would you like to force update?<html>";
				}
				showMessageDialog(d, msg, "Update?", JOptionPane.PLAIN_MESSAGE, null,
						new Btn("YES", e1 -> {
							d.dispose();
							progress(this, () -> Updater.download(), fileName -> Updater.run(fileName));
						}), new Btn("NO", e1 -> {
							d.dispose();
						}));
			});
		});

		c.connectOnUpdate((_S, _M, _AR, _PC, _DR, _AC, _IR, _TR, _SC, _E, _R, _IEN, _FGI, _FGO, _INPR, _OUTR) -> {
			S = _S;
			M = _M;
			AR = _AR;
			PC = _PC;
			DR = _DR;
			AC = _AC;
			IR = _IR;
			TR = _TR;
			SC = _SC;
			E = _E;
			R = _R;
			IEN = _IEN;
			FGI = _FGI;
			FGO = _FGO;
			INPR = _INPR;
			OUTR = _OUTR;
			t.setReg(Format.registers(S, AR, PC, DR, AC, IR, TR, SC, E, R, IEN, FGI, FGO, INPR, OUTR));
			t.setMem(Format.memory(MEM_LINES, mStart, M, PC));
			if (!_S)
				Utils.runAfter(() -> t.logS.setValue(t.logS.getMaximum()), 200);
			if (c.getAvgFrequency() > 0) {
				freqAvgF.setText(String.format("%.2f", c.getAvgFrequency()));
			}
		});
		c.runListeners();
		t.setSrc(c.getSource());

		setContentPane(p);
		updateSize();
		setTheme(isLight);
		setLocationRelativeTo(null);
		Utils.runAfter(() -> t.src.tp.requestFocus(), 500);
	}

	private String getFreq() {
		int f = c.getFrequency();
		return f < 0 ? "---" : Integer.toString(f);
	}

	private void setFreq(String s) {
		try {
			int f = Integer.parseInt(s);
			c.setFrequency(f);
		} catch (Exception e) {
			c.setFrequency(-1);
		}
	}

	private void saveProgram(String path) {
		if (path == null || !new File(path).exists()) {
			final JFileChooser fc = new JFileChooser(Utils.getDir());
			fc.setApproveButtonText("Save");
			int actionDialog = fc.showOpenDialog(this);
			if (actionDialog != JFileChooser.APPROVE_OPTION)
				return;
			path = fc.getSelectedFile().getAbsolutePath();
		}
		Utils.writeFile(path, t.getSrc(), false);
	}

	private boolean loadProgram(Txts t, String path) {
		if (path != null && !new File(path).exists()) {
			logger.log("Error: File not found \'" + path + "\'");
			return false;
		}
		String src = path == null ? t.getSrc() : Utils.readFile(path);
		return c.loadProgram(pType.getSelectedIndex(), src, path);
	}

	public void toggleTheme() {
		setTheme(isLight = !isLight);
	}

	public void setTheme(boolean light) {
		if (light) {
			bgColor = Color.WHITE;
			bgColor2 = LIGHT;
			txtColor = Color.BLACK;
		} else {
			bgColor = DARKER;
			bgColor2 = DARK;
			txtColor = Color.WHITE;
		}
		for (Themeable t : themeables)
			t.theme();
	}

	public void updateSize() {
		this.w = (int) (W * scale);
		this.h = (int) (H * scale);
		this.txtSize = (int) (TXT_SIZE * scale);
		this.editorTxtSize = (int) (EDITOR_TXT_SIZE * scale);
		setSize(w, h);
		for (Themeable t : themeables)
			t.theme();
	}

	private class Txts implements Themeable {

		JSplitPane p, p2, p3;
		JTabbedPane bottomTabs;
		Txt src, reg, mem, log, trm;
		TxtF mStartTF;
		JScrollBar logS, trmS;
		int trmI = 0;

		public Txts() {
			p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			p2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			p3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

			src = new Txt("Source", true);
			reg = new Txt("Registers");
			mem = new Txt("Memory");
			log = new Txt("", true);
			trm = new Txt("", true);

			reg.setEditable(false);
			mem.setEditable(false);
			log.setEditable(false);

			src.enableGoods();
			logS = log.getVerticalScrollBar();
			trmS = trm.getVerticalScrollBar();

			p2.setLeftComponent(src);
			p2.setRightComponent(reg);
			p3.setLeftComponent(p2);
			p3.setRightComponent(mem);

			bottomTabs = new JTabbedPane();
			bottomTabs.addTab("Terminal", trm);
			bottomTabs.addTab("Logs", log);
			addTheme(() -> {
				bottomTabs.getParent().setBackground(bgColor2);
				bottomTabs.setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
			});

			p.setTopComponent(p3);
			p.setBottomComponent(bottomTabs);

			src.addTR(new Lbl("Type: "));
			src.addTR(pType);
			pType.setSelectedIndex(c.getSourceType());
			pType.addActionListener(e -> loadProgram(this, null));
			addTheme(() -> {
				pType.setBackground(bgColor);
				pType.setForeground(txtColor);
				pType.setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
			});
			src.addTR(new Btn("Load", e -> loadProgram(this, null)));
			reg.addTR(new Btn("Clear", e -> c.clearReg()));
			mem.addTR(new Lbl("Start: "));
			mem.addTR(mStartTF = new TxtF("0", 4, e -> {
				try {
					mStart = Integer.parseInt(((TxtF) e.getSource()).getText());
					setMem(mStart);
				} catch (Exception ignored) {
				}
			}));
			mem.addTR(new Btn("↓", e -> setMem(++mStart)));
			mem.addTR(new Btn("↑", e -> setMem(--mStart)));
			mem.addTR(new Btn("Clear", e -> c.clearMem()));
			log.addTR(new Btn("Clear", e -> logger.clear()));
			log.addTR(new Btn("Save", e -> {
				final JFileChooser fc = new JFileChooser(Utils.getDir());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setApproveButtonText("Save");
				int actionDialog = fc.showOpenDialog(MainFrame.this);
				if (actionDialog != JFileChooser.APPROVE_OPTION)
					return;
				logger.save(fc.getSelectedFile().getAbsolutePath());
			}));
			trm.addTR(new Btn("Clear", e -> c.clearIO()));

			SimpleAttributeSet red = new SimpleAttributeSet();
			StyleConstants.setForeground(red, Color.RED);
			Document logD = log.getDocument();
			logger.connect(l -> {
				try {
					if (l == null) {
						log.setText("");
					} else {
						if (l.startsWith("Error:")) {
							logD.insertString(logD.getLength(), l + "\n", red);
							bottomTabs.setSelectedComponent(log);
						} else {
							logD.insertString(logD.getLength(), l + "\n", null);
						}
						logS.setValue(logS.getMaximum());
					}
				} catch (Exception ignored) {
				}
			});

			Document trmD = trm.getDocument();
			c.connectOnOut(ch -> {
				try {
					if (c.isIoCleared()) {
						trm.setText("");
						trmI = 0;
					} else if (ch != '\0') {
						trmD.insertString(trmD.getLength(), Character.toString(ch), null);
						trmS.setValue(trmS.getMaximum());
						trmI++;
					}
				} catch (Exception ignored) {
				}
			});
			JTextPane trmTp = trm.tp;
			String enter = "enter", backspace = "backspace", del = "del";
			InputMap im = trmTp.getInputMap(JComponent.WHEN_FOCUSED);
			ActionMap am = trmTp.getActionMap();
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), backspace);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), del);
			am.put(enter, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String in = trm.getText().substring(trmI);
					c.putInpStr(in);
					trmI += in.length();
					try {
						trmD.insertString(trmD.getLength(), "\n", null);
						trmI++;
					} catch (Exception ignored) {
					}
				}
			});
			am.put(backspace, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int pos = trmTp.getCaretPosition();
					if (pos > trmI)
						try {
							trmD.remove(pos - 1, 1);
						} catch (BadLocationException ignored) {
						}
				}
			});
			am.put(del, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int pos = trmTp.getCaretPosition();
					if (pos > trmI)
						try {
							trmD.remove(pos, 1);
						} catch (BadLocationException ignored) {
						}
				}
			});
			trmTp.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					trmTp.setEditable(trmTp.getCaretPosition() >= trmI);
				}
			});

			addTheme(this);
		}

		public JSplitPane getPane() {
			return p;
		}

		public void setSrc(String txt) {
			src.setText(txt);
		}

		public String getSrc() {
			return src.getText();
		}

		public void setReg(String txt) {
			reg.setText(txt);
		}

		public void setMem(String txt) {
			mem.setText(txt);
		}

		public void setMem(int mStart) {
			if (mStart < 0)
				mStart = 0;
			else if (mStart > M.length - MEM_LEN - 1)
				mStart = M.length - MEM_LEN - 1;
			MainFrame.this.mStart = mStart;
			mStartTF.setText(String.valueOf(mStart));
			setMem(Format.memory(MEM_LINES, mStart, M, PC));
		}

		@Override
		public void theme() {
			p.setDividerLocation((int) (w * 0.45));
			p2.setDividerLocation((int) (w * 0.4));
			p3.setDividerLocation((int) (w * 0.65));
		}

	}

	public final static String UNDO_ACTION = "Undo";
	public final static String REDO_ACTION = "Redo";
	public final static String ZOOM_IN = "ZoomIn";
	public final static String ZOOM_OUT = "ZoomOut";

	private class Txt extends JPanel implements Themeable {

		private static final long serialVersionUID = 1L;

		private JPanel top;
		private JPanel topRight;
		private JLabel tl;
		private JTextPane tp;
		private JScrollPane sp;

		private AbstractAction undoAction;
		private AbstractAction redoAction;

		public Txt(String title) {
			this(title, false);
		}

		public Txt(String title, boolean scroll) {
			top = new JPanel(new BorderLayout());
			tl = new JLabel(title);
			tp = new JTextPane();
			setLayout(new BorderLayout());
			top.add(tl, BorderLayout.WEST);
			top.add(topRight = new JPanel(), BorderLayout.EAST);
			add(top, BorderLayout.NORTH);
			if (scroll) {
				add(sp = new JScrollPane(tp), BorderLayout.CENTER);
			} else {
				add(tp, BorderLayout.CENTER);
			}
			addTheme(this);
		}

		@Override
		public void theme() {
			top.setBackground(bgColor2);
			topRight.setBackground(bgColor2);
			tp.setBackground(bgColor);
			if (tp.getText().length() == 0) {
				tp.setText(" ");
				tp.setForeground(txtColor);
				tp.setText("");
			} else {
				tp.setForeground(txtColor);
			}
			tp.setCaretColor(txtColor);
			tp.setFont(new Font(FONT_MONO, Font.PLAIN, editorTxtSize));
			tl.setForeground(txtColor);
			tl.setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
		}

		public void setText(String txt) {
			tp.setText(txt);
		}

		public String getText() {
			return tp.getText();
		}

		public Document getDocument() {
			return tp.getDocument();
		}

		public void setEditable(boolean b) {
			tp.setEditable(b);
		}

		public JScrollBar getVerticalScrollBar() {
			return sp == null ? null : sp.getVerticalScrollBar();
		}

		public void enableGoods() {
			enableLines();
			undoable();
			menu();
		}

		private void enableLines() {
			LineNumberingTextArea lineNumberingTextArea = new LineNumberingTextArea(tp);
			sp.setRowHeaderView(lineNumberingTextArea);
			tp.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent documentEvent) {
					lineNumberingTextArea.updateLineNumbers();
				}

				@Override
				public void removeUpdate(DocumentEvent documentEvent) {
					lineNumberingTextArea.updateLineNumbers();
				}

				@Override
				public void changedUpdate(DocumentEvent documentEvent) {
					lineNumberingTextArea.updateLineNumbers();
				}
			});
		}

		public void addTR(Component c) {
			topRight.add(c);
		}

		private void undoable() {
			final UndoManager undoMgr = new UndoManager();
			tp.getDocument().addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent pEvt) {
					undoMgr.addEdit(pEvt.getEdit());
				}
			});
			tp.getActionMap().put(UNDO_ACTION, undoAction = new AbstractAction(UNDO_ACTION) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent pEvt) {
					try {
						if (undoMgr.canUndo()) {
							undoMgr.undo();
						}
					} catch (CannotUndoException e) {
						e.printStackTrace();
					}
				}
			});
			tp.getActionMap().put(REDO_ACTION, redoAction = new AbstractAction(REDO_ACTION) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent pEvt) {
					try {
						if (undoMgr.canRedo()) {
							undoMgr.redo();
						}
					} catch (CannotRedoException e) {
						e.printStackTrace();
					}
				}
			});
			tp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), UNDO_ACTION);
			tp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REDO_ACTION);
			tp.getInputMap().put(
					KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
					REDO_ACTION);
			tp.getActionMap().put(ZOOM_IN, redoAction = new AbstractAction(ZOOM_IN) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent pEvt) {
					if (scale > 10)
						return;
					scale += 0.1;
					scaleF.setText(Float.toString(scale));
					updateSize();
				}
			});
			tp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), ZOOM_IN);
			tp.getActionMap().put(ZOOM_OUT, redoAction = new AbstractAction(ZOOM_OUT) {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent pEvt) {
					if (scale < 0)
						return;
					scale -= 0.1;
					scaleF.setText(Float.toString(scale));
					updateSize();
				}
			});
			tp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), ZOOM_OUT);
		}

		private void menu() {
			JPopupMenu menu = new JPopupMenu();
			Action cut = new DefaultEditorKit.CutAction();
			cut.putValue(Action.NAME, "Cut");
			cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
			menu.add(cut);
			Action copy = new DefaultEditorKit.CopyAction();
			copy.putValue(Action.NAME, "Copy");
			copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
			menu.add(copy);
			Action paste = new DefaultEditorKit.PasteAction();
			paste.putValue(Action.NAME, "Paste");
			paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
			menu.add(paste);
			Action all = new TextAction("Select All") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					JTextComponent component = getFocusedComponent();
					component.selectAll();
					component.requestFocusInWindow();
				}
			};
			menu.add(all);
			all.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
			Action undo = undoAction;
			undo.putValue(Action.NAME, "Undo");
			undo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
			menu.add(undo);
			Action redo = redoAction;
			redo.putValue(Action.NAME, "Redo");
			redo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
			menu.add(redo);
			tp.setComponentPopupMenu(menu);
		}

	}

	private class LineNumberingTextArea extends JTextArea implements Themeable {

		private static final long serialVersionUID = 1L;

		private JTextPane textPane;

		public LineNumberingTextArea(JTextPane textPane) {
			this.textPane = textPane;
			setEditable(false);
			addTheme(this);
		}

		public void updateLineNumbers() {
			String lineNumbersText = getLineNumbersText();
			setText(lineNumbersText);
		}

		private String getLineNumbersText() {
			int caretPosition = textPane.getDocument().getLength();
			Element root = textPane.getDocument().getDefaultRootElement();
			StringBuilder lineNumbersTextBuilder = new StringBuilder();
			lineNumbersTextBuilder.append("1").append(System.lineSeparator());

			for (int elementIndex = 2; elementIndex < root.getElementIndex(caretPosition) + 2; elementIndex++) {
				lineNumbersTextBuilder.append(elementIndex).append(System.lineSeparator());
			}

			return lineNumbersTextBuilder.toString();
		}

		@Override
		public void theme() {
			setBackground(bgColor);
			setForeground(Color.GRAY);
			setFont(new Font(FONT_MONO, Font.PLAIN, editorTxtSize));
		}
	}

	private class TxtF extends JTextField implements Themeable {

		private static final long serialVersionUID = 1L;

		public TxtF(String s, int cols) {
			this(s, cols, null);
		}

		public TxtF(String s, int cols, ActionListener l) {
			super(s, cols);
			if (l != null)
				addActionListener(l);
			addTheme(this);
		}

		@Override
		public void theme() {
			setBackground(bgColor);
			setForeground(txtColor);
			setCaretColor(txtColor);
			setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
		}

	}

	private class Btn extends JButton implements Themeable {

		private static final long serialVersionUID = 1L;

		public Btn(String txt) {
			this(txt, null);
		}

		public Btn(String txt, ActionListener l) {
			super(txt);
			if (l != null)
				addActionListener(l);
			addTheme(this);
		}

		@Override
		public void theme() {
			setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
			setBackground(bgColor);
			setForeground(txtColor);
		}
	}

	private class Lbl extends JLabel implements Themeable {

		private static final long serialVersionUID = 1L;

		public Lbl(String txt) {
			super(txt);
			addTheme(this);
		}

		@Override
		public void theme() {
			setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize));
			setForeground(txtColor);
		}
	}

	private class LoadingDialog extends JDialog implements Themeable {

		private static final long serialVersionUID = 1L;

		Lbl l;
		JPanel p;

		public LoadingDialog(Frame owner) {
			super(owner);
			p = new JPanel(new BorderLayout());
			p.add(l = new Lbl("Please wait..."), BorderLayout.CENTER);
			setUndecorated(true);
			getContentPane().add(p);
			setLocationRelativeTo(this);
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			setModal(true);
			theme();
		}

		@Override
		public void theme() {
			l.theme();
			l.setFont(new Font(FONT_NORMAL, Font.PLAIN, txtSize * 2));
			p.setBackground(bgColor);
			pack();
			setLocationRelativeTo(null);
		}
	}

	public interface SimpleTaskIn<O> {
		O run();
	}

	public interface SimpleTaskOut<O> {
		void run(O out);
	}

	private <O> void progress(Frame owner, SimpleTaskIn<O> async, SimpleTaskOut<O> post) {
		final JDialog loading = new LoadingDialog(owner);

		SwingWorker<O, Void> worker = new SwingWorker<O, Void>() {
			@Override
			protected O doInBackground() throws InterruptedException {
				if (async != null)
					return async.run();
				return null;
			}

			@Override
			protected void done() {
				loading.dispose();
				if (post != null)
					try {
						post.run(get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
			}
		};
		worker.execute();
		loading.setVisible(true);
		try {
			worker.get();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void showMessageDialog(JDialog d, String message, String title, int messageType, Icon icon, Btn... btns) {
		d.setTitle(title);
		d.setModal(true);
		d.setResizable(false);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(bgColor);
		Lbl label = new Lbl(message);
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.theme();
		if (icon != null)
			label.setIcon(icon);
		panel.add(label, BorderLayout.CENTER);
		JPanel btnsP = new JPanel();
		btnsP.setBackground(bgColor);
		panel.add(btnsP, BorderLayout.SOUTH);
		for (int i = 0; i < btns.length; ++i) {
			btns[i].theme();
			btnsP.add(btns[i]);
		}
		d.setContentPane(panel);
		d.pack();
		d.setLocationRelativeTo(null);
		d.setVisible(true);
	}

	private List<Themeable> themeables = new ArrayList<>();

	private interface Themeable {
		void theme();
	}

	private void addTheme(Themeable t) {
		themeables.add(t);
	}

}
