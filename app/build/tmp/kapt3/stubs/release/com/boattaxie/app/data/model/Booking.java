package com.boattaxie.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Booking/Trip model for ride requests
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b}\b\u0086\b\u0018\u00002\u00020\u0001B\u009f\u0004\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\f\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0011\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\'\u001a\u00020(\u0012\b\b\u0002\u0010)\u001a\u00020(\u0012\b\b\u0002\u0010*\u001a\u00020+\u0012\n\b\u0002\u0010,\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\u0011\u0012\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u0011\u0012\n\b\u0002\u00100\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u00102\u001a\u0004\u0018\u00010\u0003\u0012\u000e\b\u0002\u00103\u001a\b\u0012\u0004\u0012\u00020\f04\u0012\b\b\u0002\u00105\u001a\u000206\u0012\n\b\u0002\u00107\u001a\u0004\u0018\u000106\u0012\n\b\u0002\u00108\u001a\u0004\u0018\u000106\u0012\n\b\u0002\u00109\u001a\u0004\u0018\u000106\u0012\n\b\u0002\u0010:\u001a\u0004\u0018\u000106\u0012\n\b\u0002\u0010;\u001a\u0004\u0018\u000106\u00a2\u0006\u0002\u0010<J\t\u0010~\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u007f\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0080\u0001\u001a\u00020\u0011H\u00c6\u0003J\n\u0010\u0081\u0001\u001a\u00020\u0013H\u00c6\u0003J\n\u0010\u0082\u0001\u001a\u00020\u0015H\u00c6\u0003J\u0011\u0010\u0083\u0001\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010IJ\f\u0010\u0084\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0085\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0086\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0087\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0088\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\n\u0010\u0089\u0001\u001a\u00020\u0003H\u00c6\u0003J\f\u0010\u008a\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u008b\u0001\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003\u00a2\u0006\u0002\u0010RJ\u0011\u0010\u008c\u0001\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u0010WJ\f\u0010\u008d\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u008e\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u008f\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0090\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0091\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0092\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u0093\u0001\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010IJ\f\u0010\u0094\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u0095\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\n\u0010\u0096\u0001\u001a\u00020(H\u00c6\u0003J\n\u0010\u0097\u0001\u001a\u00020(H\u00c6\u0003J\n\u0010\u0098\u0001\u001a\u00020+H\u00c6\u0003J\f\u0010\u0099\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u009a\u0001\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003\u00a2\u0006\u0002\u0010RJ\f\u0010\u009b\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u009c\u0001\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003\u00a2\u0006\u0002\u0010RJ\f\u0010\u009d\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\f\u0010\u009e\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\n\u0010\u009f\u0001\u001a\u00020\u0007H\u00c6\u0003J\f\u0010\u00a0\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010\u00a1\u0001\u001a\b\u0012\u0004\u0012\u00020\f04H\u00c6\u0003J\n\u0010\u00a2\u0001\u001a\u000206H\u00c6\u0003J\f\u0010\u00a3\u0001\u001a\u0004\u0018\u000106H\u00c6\u0003J\f\u0010\u00a4\u0001\u001a\u0004\u0018\u000106H\u00c6\u0003J\f\u0010\u00a5\u0001\u001a\u0004\u0018\u000106H\u00c6\u0003J\f\u0010\u00a6\u0001\u001a\u0004\u0018\u000106H\u00c6\u0003J\f\u0010\u00a7\u0001\u001a\u0004\u0018\u000106H\u00c6\u0003J\f\u0010\u00a8\u0001\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\n\u0010\u00a9\u0001\u001a\u00020\nH\u00c6\u0003J\n\u0010\u00aa\u0001\u001a\u00020\fH\u00c6\u0003J\n\u0010\u00ab\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u00ac\u0001\u001a\u00020\fH\u00c6\u0003J\u00aa\u0004\u0010\u00ad\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u00032\b\b\u0002\u0010\u000e\u001a\u00020\f2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u00152\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\'\u001a\u00020(2\b\b\u0002\u0010)\u001a\u00020(2\b\b\u0002\u0010*\u001a\u00020+2\n\b\u0002\u0010,\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010-\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u0010.\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010/\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u00100\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u00102\u001a\u0004\u0018\u00010\u00032\u000e\b\u0002\u00103\u001a\b\u0012\u0004\u0012\u00020\f042\b\b\u0002\u00105\u001a\u0002062\n\b\u0002\u00107\u001a\u0004\u0018\u0001062\n\b\u0002\u00108\u001a\u0004\u0018\u0001062\n\b\u0002\u00109\u001a\u0004\u0018\u0001062\n\b\u0002\u0010:\u001a\u0004\u0018\u0001062\n\b\u0002\u0010;\u001a\u0004\u0018\u000106H\u00c6\u0001\u00a2\u0006\u0003\u0010\u00ae\u0001J\u0015\u0010\u00af\u0001\u001a\u00020(2\t\u0010\u00b0\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\n\u0010\u00b1\u0001\u001a\u00020\u0013H\u00d6\u0001J\n\u0010\u00b2\u0001\u001a\u00020\u0003H\u00d6\u0001R\u0013\u00107\u001a\u0004\u0018\u000106\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010>R\u0013\u00108\u001a\u0004\u0018\u000106\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010>R\u0013\u00102\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010AR\u0013\u0010;\u001a\u0004\u0018\u000106\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010>R\u0013\u00101\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010AR\u0013\u0010:\u001a\u0004\u0018\u000106\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010>R\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010AR\u0011\u0010\u000e\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010GR\u0015\u0010%\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\n\n\u0002\u0010J\u001a\u0004\bH\u0010IR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010AR\u0013\u0010\u001f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u0010AR\u0013\u0010 \u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010AR\u0013\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u0010AR\u0013\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010AR\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010AR\u0015\u0010/\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\n\n\u0002\u0010S\u001a\u0004\bQ\u0010RR\u0015\u0010\u001d\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\n\n\u0002\u0010S\u001a\u0004\bT\u0010RR\u0013\u00100\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010AR\u0015\u0010\u001e\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u0010X\u001a\u0004\bV\u0010WR\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\bY\u0010ZR\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b[\u0010\\R\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b]\u0010^R\u0013\u0010&\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b_\u0010AR\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\n\n\u0002\u0010J\u001a\u0004\b`\u0010IR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\ba\u0010AR\u001e\u0010)\u001a\u00020(8\u0007@\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010b\"\u0004\bc\u0010dR\u0013\u0010,\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\be\u0010AR\u0011\u0010*\u001a\u00020+\u00a2\u0006\b\n\u0000\u001a\u0004\bf\u0010gR\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bh\u0010AR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\bi\u0010GR\u0015\u0010-\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\n\n\u0002\u0010S\u001a\u0004\bj\u0010RR\u0011\u00105\u001a\u000206\u00a2\u0006\b\n\u0000\u001a\u0004\bk\u0010>R\u0013\u0010.\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bl\u0010AR\u0011\u0010\'\u001a\u00020(\u00a2\u0006\b\n\u0000\u001a\u0004\bm\u0010bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bn\u0010AR\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bo\u0010AR\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bp\u0010AR\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bq\u0010AR\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\f04\u00a2\u0006\b\n\u0000\u001a\u0004\br\u0010sR\u0013\u00109\u001a\u0004\u0018\u000106\u00a2\u0006\b\n\u0000\u001a\u0004\bt\u0010>R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bu\u0010vR\u0013\u0010#\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bw\u0010AR\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bx\u0010AR\u0013\u0010\"\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\by\u0010AR\u0013\u0010$\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bz\u0010AR\u0013\u0010!\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b{\u0010AR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b|\u0010}\u00a8\u0006\u00b3\u0001"}, d2 = {"Lcom/boattaxie/app/data/model/Booking;", "", "id", "", "riderId", "driverId", "vehicleType", "Lcom/boattaxie/app/data/model/VehicleType;", "vehicleId", "status", "Lcom/boattaxie/app/data/model/BookingStatus;", "pickupLocation", "Lcom/boattaxie/app/data/model/GeoLocation;", "pickupAddress", "destinationLocation", "destinationAddress", "estimatedDistance", "", "estimatedDuration", "", "estimatedFare", "", "finalFare", "riderName", "riderPhoneNumber", "riderPhotoUrl", "driverName", "driverPhoneNumber", "driverPhotoUrl", "driverRatingValue", "driverTotalTrips", "driverLicenseNumber", "driverLicenseType", "vehiclePlate", "vehicleModel", "vehicleColor", "vehiclePhoto", "driverAdjustedFare", "fareAdjustmentReason", "riderAcceptedAdjustment", "", "isNightRate", "paymentStatus", "Lcom/boattaxie/app/data/model/PaymentStatus;", "paymentMethod", "rating", "review", "driverRating", "driverReview", "cancelledBy", "cancellationReason", "route", "", "requestedAt", "Lcom/google/firebase/Timestamp;", "acceptedAt", "arrivedAt", "startedAt", "completedAt", "cancelledAt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/VehicleType;Ljava/lang/String;Lcom/boattaxie/app/data/model/BookingStatus;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;FIDLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;ZZLcom/boattaxie/app/data/model/PaymentStatus;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;)V", "getAcceptedAt", "()Lcom/google/firebase/Timestamp;", "getArrivedAt", "getCancellationReason", "()Ljava/lang/String;", "getCancelledAt", "getCancelledBy", "getCompletedAt", "getDestinationAddress", "getDestinationLocation", "()Lcom/boattaxie/app/data/model/GeoLocation;", "getDriverAdjustedFare", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getDriverId", "getDriverLicenseNumber", "getDriverLicenseType", "getDriverName", "getDriverPhoneNumber", "getDriverPhotoUrl", "getDriverRating", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getDriverRatingValue", "getDriverReview", "getDriverTotalTrips", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getEstimatedDistance", "()F", "getEstimatedDuration", "()I", "getEstimatedFare", "()D", "getFareAdjustmentReason", "getFinalFare", "getId", "()Z", "setNightRate", "(Z)V", "getPaymentMethod", "getPaymentStatus", "()Lcom/boattaxie/app/data/model/PaymentStatus;", "getPickupAddress", "getPickupLocation", "getRating", "getRequestedAt", "getReview", "getRiderAcceptedAdjustment", "getRiderId", "getRiderName", "getRiderPhoneNumber", "getRiderPhotoUrl", "getRoute", "()Ljava/util/List;", "getStartedAt", "getStatus", "()Lcom/boattaxie/app/data/model/BookingStatus;", "getVehicleColor", "getVehicleId", "getVehicleModel", "getVehiclePhoto", "getVehiclePlate", "getVehicleType", "()Lcom/boattaxie/app/data/model/VehicleType;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component32", "component33", "component34", "component35", "component36", "component37", "component38", "component39", "component4", "component40", "component41", "component42", "component43", "component44", "component45", "component46", "component47", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/boattaxie/app/data/model/VehicleType;Ljava/lang/String;Lcom/boattaxie/app/data/model/BookingStatus;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;Lcom/boattaxie/app/data/model/GeoLocation;Ljava/lang/String;FIDLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;ZZLcom/boattaxie/app/data/model/PaymentStatus;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;Lcom/google/firebase/Timestamp;)Lcom/boattaxie/app/data/model/Booking;", "equals", "other", "hashCode", "toString", "app_release"})
public final class Booking {
    @com.google.firebase.firestore.DocumentId()
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String riderId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.VehicleType vehicleType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehicleId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.BookingStatus status = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.GeoLocation pickupLocation = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String pickupAddress = null;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.GeoLocation destinationLocation = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String destinationAddress = null;
    private final float estimatedDistance = 0.0F;
    private final int estimatedDuration = 0;
    private final double estimatedFare = 0.0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double finalFare = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String riderName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String riderPhoneNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String riderPhotoUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverPhoneNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverPhotoUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float driverRatingValue = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer driverTotalTrips = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverLicenseNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverLicenseType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehiclePlate = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehicleModel = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehicleColor = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String vehiclePhoto = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double driverAdjustedFare = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fareAdjustmentReason = null;
    private final boolean riderAcceptedAdjustment = false;
    private boolean isNightRate;
    @org.jetbrains.annotations.NotNull()
    private final com.boattaxie.app.data.model.PaymentStatus paymentStatus = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String paymentMethod = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float rating = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String review = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float driverRating = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String driverReview = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String cancelledBy = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String cancellationReason = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.boattaxie.app.data.model.GeoLocation> route = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.Timestamp requestedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp acceptedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp arrivedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp startedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp completedAt = null;
    @org.jetbrains.annotations.Nullable()
    private final com.google.firebase.Timestamp cancelledAt = null;
    
    public Booking(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String riderId, @org.jetbrains.annotations.Nullable()
    java.lang.String driverId, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleId, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.BookingStatus status, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.GeoLocation pickupLocation, @org.jetbrains.annotations.NotNull()
    java.lang.String pickupAddress, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.GeoLocation destinationLocation, @org.jetbrains.annotations.NotNull()
    java.lang.String destinationAddress, float estimatedDistance, int estimatedDuration, double estimatedFare, @org.jetbrains.annotations.Nullable()
    java.lang.Double finalFare, @org.jetbrains.annotations.Nullable()
    java.lang.String riderName, @org.jetbrains.annotations.Nullable()
    java.lang.String riderPhoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String riderPhotoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String driverName, @org.jetbrains.annotations.Nullable()
    java.lang.String driverPhoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String driverPhotoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.Float driverRatingValue, @org.jetbrains.annotations.Nullable()
    java.lang.Integer driverTotalTrips, @org.jetbrains.annotations.Nullable()
    java.lang.String driverLicenseNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String driverLicenseType, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePlate, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleModel, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleColor, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePhoto, @org.jetbrains.annotations.Nullable()
    java.lang.Double driverAdjustedFare, @org.jetbrains.annotations.Nullable()
    java.lang.String fareAdjustmentReason, boolean riderAcceptedAdjustment, boolean isNightRate, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.PaymentStatus paymentStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String paymentMethod, @org.jetbrains.annotations.Nullable()
    java.lang.Float rating, @org.jetbrains.annotations.Nullable()
    java.lang.String review, @org.jetbrains.annotations.Nullable()
    java.lang.Float driverRating, @org.jetbrains.annotations.Nullable()
    java.lang.String driverReview, @org.jetbrains.annotations.Nullable()
    java.lang.String cancelledBy, @org.jetbrains.annotations.Nullable()
    java.lang.String cancellationReason, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.GeoLocation> route, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp requestedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp acceptedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp arrivedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp startedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp completedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp cancelledAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRiderId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType getVehicleType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehicleId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.BookingStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.GeoLocation getPickupLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPickupAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.GeoLocation getDestinationLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDestinationAddress() {
        return null;
    }
    
    public final float getEstimatedDistance() {
        return 0.0F;
    }
    
    public final int getEstimatedDuration() {
        return 0;
    }
    
    public final double getEstimatedFare() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getFinalFare() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRiderName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRiderPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRiderPhotoUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverPhotoUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getDriverRatingValue() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getDriverTotalTrips() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverLicenseNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverLicenseType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehiclePlate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehicleModel() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehicleColor() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVehiclePhoto() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getDriverAdjustedFare() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFareAdjustmentReason() {
        return null;
    }
    
    public final boolean getRiderAcceptedAdjustment() {
        return false;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "nightRate")
    public final boolean isNightRate() {
        return false;
    }
    
    @com.google.firebase.firestore.PropertyName(value = "nightRate")
    public final void setNightRate(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.PaymentStatus getPaymentStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPaymentMethod() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getRating() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getReview() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getDriverRating() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDriverReview() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCancelledBy() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCancellationReason() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.GeoLocation> getRoute() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp getRequestedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getAcceptedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getArrivedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getStartedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getCompletedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp getCancelledAt() {
        return null;
    }
    
    public Booking() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    public final float component11() {
        return 0.0F;
    }
    
    public final int component12() {
        return 0;
    }
    
    public final double component13() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component21() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component26() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component27() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component29() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component30() {
        return null;
    }
    
    public final boolean component31() {
        return false;
    }
    
    public final boolean component32() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.PaymentStatus component33() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component34() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component35() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component36() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component37() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component38() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component39() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.VehicleType component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component40() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.boattaxie.app.data.model.GeoLocation> component41() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.Timestamp component42() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component43() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component44() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component45() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component46() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.Timestamp component47() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.BookingStatus component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.GeoLocation component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.GeoLocation component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.boattaxie.app.data.model.Booking copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String riderId, @org.jetbrains.annotations.Nullable()
    java.lang.String driverId, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.VehicleType vehicleType, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleId, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.BookingStatus status, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.GeoLocation pickupLocation, @org.jetbrains.annotations.NotNull()
    java.lang.String pickupAddress, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.GeoLocation destinationLocation, @org.jetbrains.annotations.NotNull()
    java.lang.String destinationAddress, float estimatedDistance, int estimatedDuration, double estimatedFare, @org.jetbrains.annotations.Nullable()
    java.lang.Double finalFare, @org.jetbrains.annotations.Nullable()
    java.lang.String riderName, @org.jetbrains.annotations.Nullable()
    java.lang.String riderPhoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String riderPhotoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String driverName, @org.jetbrains.annotations.Nullable()
    java.lang.String driverPhoneNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String driverPhotoUrl, @org.jetbrains.annotations.Nullable()
    java.lang.Float driverRatingValue, @org.jetbrains.annotations.Nullable()
    java.lang.Integer driverTotalTrips, @org.jetbrains.annotations.Nullable()
    java.lang.String driverLicenseNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String driverLicenseType, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePlate, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleModel, @org.jetbrains.annotations.Nullable()
    java.lang.String vehicleColor, @org.jetbrains.annotations.Nullable()
    java.lang.String vehiclePhoto, @org.jetbrains.annotations.Nullable()
    java.lang.Double driverAdjustedFare, @org.jetbrains.annotations.Nullable()
    java.lang.String fareAdjustmentReason, boolean riderAcceptedAdjustment, boolean isNightRate, @org.jetbrains.annotations.NotNull()
    com.boattaxie.app.data.model.PaymentStatus paymentStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String paymentMethod, @org.jetbrains.annotations.Nullable()
    java.lang.Float rating, @org.jetbrains.annotations.Nullable()
    java.lang.String review, @org.jetbrains.annotations.Nullable()
    java.lang.Float driverRating, @org.jetbrains.annotations.Nullable()
    java.lang.String driverReview, @org.jetbrains.annotations.Nullable()
    java.lang.String cancelledBy, @org.jetbrains.annotations.Nullable()
    java.lang.String cancellationReason, @org.jetbrains.annotations.NotNull()
    java.util.List<com.boattaxie.app.data.model.GeoLocation> route, @org.jetbrains.annotations.NotNull()
    com.google.firebase.Timestamp requestedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp acceptedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp arrivedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp startedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp completedAt, @org.jetbrains.annotations.Nullable()
    com.google.firebase.Timestamp cancelledAt) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}