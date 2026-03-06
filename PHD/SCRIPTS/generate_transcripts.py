cat > /Users/nickbowditch/Documents/PHD/SCRIPTS/generate_transcripts_v8.py <<'PYCODE'
import os, random, pandas as pd
from faker import Faker

fake = Faker("en_AU")
random.seed(42)

output_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_transcripts_v8.xlsx"

# --- settings ---
participants = [f"P-V8-{i:03d}" for i in range(1, 251)]
weeks = ["baseline"] + [f"voicenote{i}" for i in range(1, 12)] + ["exit"]
themes = [
    "orientation and early insight", "craving management", "self-efficacy",
    "motivation", "thinking shifts", "emotional tone", "hope and progress",
    "connection", "internal battle", "long-term motivation", "final reflection"
]

# --- helpers ---
def make_sentence(theme, tone):
    starts = [
        "This week", "Lately", "Sometimes", "I’ve noticed", "I feel", "Looking back",
        "At first", "Recently", "Each day", "To be honest"
    ]
    mids = {
        "positive": [
            "I managed to stay grounded even when stressed",
            "talking to my mentor made the cravings fade",
            "small wins reminded me recovery is possible",
            "I’m proud of how far I’ve come this week",
            "I surprised myself by staying calm through chaos",
        ],
        "neutral": [
            "things felt steady but also a bit repetitive",
            "I’ve been trying to keep my focus on small habits",
            "some days feel good, others drag on",
            "I kept busy to stay distracted from old urges",
            "there’s a rhythm to recovery I’m starting to accept",
        ],
        "negative": [
            "I felt detached and unsure of what’s next",
            "old thoughts keep creeping in when I’m alone",
            "it’s hard to feel proud when everything feels heavy",
            "I missed the structure of meetings this week",
            "I wanted to give up a few times but didn’t",
        ],
    }
    enders = [
        "but I’m learning from it", "and that’s okay", "yet I keep trying",
        "and I know this is part of the process", "so I’ll keep showing up"
    ]
    tone_list = mids[tone]
    s = f"{random.choice(starts)} {random.choice(tone_list)}, {random.choice(enders)}."
    return s

def build_transcript(theme, dropout, relapse):
    tone = (
        "negative" if dropout or relapse else
        random.choices(["positive", "neutral"], [0.6, 0.4])[0]
    )
    txt = " ".join(make_sentence(theme, tone) for _ in range(25))
    return txt

# --- data ---
rows = []
for pid in participants:
    dropout = random.choice([True, False])
    relapse = random.choice([True, False])
    for i, week in enumerate(weeks):
        if dropout and week not in ["baseline"] and random.random() > 0.3:
            break  # stop creating more after dropout
        theme = "baseline interview" if week == "baseline" else (
            "exit interview" if week == "exit" else themes[min(i - 1, len(themes) - 1)]
        )
        txt = build_transcript(theme, dropout, relapse)
        rows.append({
            "participant_id": pid,
            "transcript_type": week,
            "transcript": txt,
            "DTCQ-8": random.randint(25, 100),
            "fused_DTCQ-8": "",
            "URICA-S": random.randint(25, 100),
            "fused_URICA-S": "",
            "BAM-R": random.randint(25, 100),
            "fused_BAM-R": "",
            "dropout_actual": "Y" if dropout else "N",
            "relapse_actual": "Y" if relapse else "N"
        })

df = pd.DataFrame(rows)
os.makedirs(os.path.dirname(output_path), exist_ok=True)
df.to_excel(output_path, index=False)
print(f"✅ Generated {len(df)} transcripts and saved to {output_path}")
PYCODE