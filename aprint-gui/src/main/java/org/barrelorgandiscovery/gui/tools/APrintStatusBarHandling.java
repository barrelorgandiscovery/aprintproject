package org.barrelorgandiscovery.gui.tools;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.barrelorgandiscovery.gui.aprintng.IStatusBarFeedBackTransactional;

public class APrintStatusBarHandling implements IStatusBarFeedBackTransactional, AutoCloseable {

	JLabel statusbar;

	Timer timer;

	public enum MessageStatus {
		NORMAL, STALLED,
	}

	public static class StatusBarTransaction {
		String transactionId;
		String latestText;
		Double Progress = null;
		long startTime;
		long latestModification = System.currentTimeMillis();
		MessageStatus status = MessageStatus.NORMAL;
	}

	StatusBarTransaction currentGeneral = null;

	ArrayList<StatusBarTransaction> transactions = new ArrayList<>();

	static long displayMillis = 2_000;

	static long transactionStalledTimeout = 10_000;

	public APrintStatusBarHandling(JLabel statusbarLocation) {
		assert statusbarLocation != null;
		this.statusbar = statusbarLocation;
		this.timer = new Timer(1000, (e) -> timerActionHandler(e));
		this.timer.setRepeats(true);
		this.timer.start();
	}

	@Override
	public void close() throws Exception {
		this.timer.stop();
	}

	private void timerActionHandler(ActionEvent event) {

		ArrayList<StatusBarTransaction> newTransactions = new ArrayList<>();
		for (StatusBarTransaction t : transactions) {

			newTransactions.add(t);

			if (t.status == MessageStatus.STALLED) {
				if ((System.currentTimeMillis() - t.latestModification) > transactionStalledTimeout) {
					// remove the transaction
					newTransactions.remove(t);
				}
			}

			if ((System.currentTimeMillis() - t.latestModification > displayMillis)) {
				t.status = MessageStatus.STALLED;
			}
		}
		this.transactions = newTransactions;
		refresh();
	}

	private boolean isValid(StatusBarTransaction transaction) {
		if (transaction == null) {
			return false;
		}
		assert transaction != null;
		if ((System.currentTimeMillis() - transaction.latestModification) > displayMillis) {
			return false;
		}
		return true;
	}

	private void refresh() {
		String latestText = "";
		if (isValid(currentGeneral)) {
			latestText = currentGeneral.latestText;
			if (latestText == null) {
				latestText = "";
			}

		} else {
			currentGeneral = null;
		}

		// watch transactions
		for (StatusBarTransaction t : transactions) {

			latestText += " " + (t.latestText == null ? "" : t.latestText);
			if (t.Progress != null) {
				latestText += String.format(" (%2d%%)", (int) (t.Progress * 100));
			}

			if (t.status == MessageStatus.STALLED) {
				latestText += " NotResponding";
			}

		}

		this.statusbar.setText(latestText);
		this.statusbar.repaint();
	}

	private void touchTransaction(StatusBarTransaction t) {
		if (t == null) {
			return;
		}
		t.latestModification = System.currentTimeMillis();
		t.status = MessageStatus.NORMAL;
	}

	@Override
	public void generalInformation(String usertext) {
		StatusBarTransaction t = new StatusBarTransaction();
		t.latestText = usertext;
		t.startTime = System.currentTimeMillis();
		touchTransaction(t);
		this.currentGeneral = t;
	}

	@Override
	public StatusBarTransaction startTransaction() {
		StatusBarTransaction t = new StatusBarTransaction();
		t.latestText = "";
		t.startTime = System.currentTimeMillis();
		transactions.add(t);
		return t;
	}

	@Override
	public void transactionProgress(StatusBarTransaction transaction, double progress) {
		if (!transactions.contains(transaction)) {
			return;
		}
		transaction.Progress = progress;
		touchTransaction(transaction);

		refresh();
	}

	@Override
	public void transactionText(StatusBarTransaction transaction, String text) {
		if (!transactions.contains(transaction)) {
			return;
		}
		transaction.latestText = text;
		touchTransaction(transaction);

		refresh();
	}

	@Override
	public void endTransaction(StatusBarTransaction transaction) {
		if (!transactions.contains(transaction)) {
			return;
		}
		transactions.remove(transaction);
		refresh();
	}

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		JLabel status = new JLabel();
		status.setPreferredSize(new Dimension(500, 16));

		f.getContentPane().setLayout(new FlowLayout());
		f.getContentPane().add(status);

		AtomicReference<StatusBarTransaction> t = new AtomicReference();

		APrintStatusBarHandling b = new APrintStatusBarHandling(status);
		JButton button = new JButton("start transaction");
		button.addActionListener((e) -> {
			StatusBarTransaction startTransaction = b.startTransaction();
			b.transactionText(startTransaction, "transaction " + System.currentTimeMillis());
			StatusBarTransaction old = t.getAndSet(startTransaction);
		});

		JButton button2 = new JButton("touchTransaction");
		button2.addActionListener((e) -> {
			StatusBarTransaction g = t.get();
			if (g != null) {
				if (g.Progress == null) {
					b.transactionProgress(g, 0.1);
				} else {
					b.transactionProgress(g, g.Progress + 0.1);
				}
			}
		});

		f.getContentPane().add(button);
		f.getContentPane().add(button2);

		f.setSize(500, 300);
		f.setVisible(true);

	}

}
